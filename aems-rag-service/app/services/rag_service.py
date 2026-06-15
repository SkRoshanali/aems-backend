"""
RAG Query Service with Role-Based Filtering
"""
from langchain_openai import OpenAIEmbeddings, ChatOpenAI
from langchain_community.vectorstores import PGVector
from langchain.chains import RetrievalQA
from langchain.prompts import PromptTemplate
from typing import Dict, List, Optional
from ..config import settings
from ..database import get_connection_string
import logging

logger = logging.getLogger(__name__)


class RAGService:
    
    def __init__(self):
        self.embeddings = OpenAIEmbeddings(
            model=settings.OPENAI_EMBEDDING_MODEL,
            openai_api_key=settings.OPENAI_API_KEY
        )
        
        self.llm = ChatOpenAI(
            model=settings.OPENAI_CHAT_MODEL,
            temperature=0.7,
            openai_api_key=settings.OPENAI_API_KEY
        )
        
        self.vector_store = PGVector(
            connection_string=get_connection_string(),
            embedding_function=self.embeddings,
            collection_name="document_chunks"
        )
    
    def build_metadata_filter(
        self, 
        role: str, 
        buyer_status: Optional[str] = None,
        buyer_id: Optional[str] = None
    ) -> Optional[Dict]:
        """
        Build role-based metadata filter for vector search
        
        Args:
            role: User role (BUYER, EMPLOYEE, MANAGER, ADMIN, SUPER_ADMIN)
            buyer_status: For BUYER role - "PENDING" or "ACCEPTED"
            buyer_id: Specific buyer ID for personal documents
        
        Returns:
            Metadata filter dict or None for admin (no filter)
        """
        if role == "BUYER":
            if buyer_status == "PENDING":
                # Pending buyers: only see public catalog/onboarding
                return {"visibility": "public"}
            elif buyer_status == "ACCEPTED" and buyer_id:
                # Active buyers: public catalog + their own orders
                return {
                    "$or": [
                        {"visibility": "public"},
                        {"visibility": f"buyer:{buyer_id}"}
                    ]
                }
            else:
                return {"visibility": "public"}
        
        elif role == "EMPLOYEE":
            # Employees: internal procedures + public catalog
            return {
                "$or": [
                    {"visibility": "internal"},
                    {"visibility": "public"}
                ]
            }
        
        elif role == "MANAGER":
            # Managers: internal + management dashboards + public
            return {
                "$or": [
                    {"visibility": "internal"},
                    {"visibility": "management"},
                    {"visibility": "public"}
                ]
            }
        
        elif role in ["ADMIN", "SUPER_ADMIN"]:
            # Admins see everything
            return None
        
        else:
            # Default: only public
            return {"visibility": "public"}
    
    def get_role_system_prompt(self, role: str) -> str:
        """Get role-specific system prompt for LLM"""
        prompts = {
            "SUPER_ADMIN": """You are an AI assistant for the Super Administrator of the Agri Export Management System (AEMS).

You have access to:
- All system documentation and configurations
- Financial reports and analytics
- User management and audit logs
- Operational procedures and workflows

Your responses should be:
- Detailed and technical
- Include relevant metrics and data
- Provide actionable insights
- Mention security and compliance considerations""",
            
            "ADMIN": """You are an AI assistant for System Administrators of AEMS.

You have access to:
- Operational procedures and guidelines
- User management documentation
- Order and inventory reports
- System configuration guides

Your responses should be:
- Practical and actionable
- Include step-by-step instructions when relevant
- Reference specific system features
- Help optimize daily operations""",
            
            "MANAGER": """You are an AI assistant for Managers in AEMS.

You have access to:
- Buyer application summaries
- Inventory and stock reports
- Order management workflows
- Team procedures and guidelines

Your responses should be:
- Business-focused
- Help with decision-making
- Provide relevant statistics
- Explain approval processes clearly""",
            
            "EMPLOYEE": """You are an AI assistant for Employees in AEMS.

You have access to:
- Training materials
- Daily operation guides
- Farmer and stock management procedures
- Product information

Your responses should be:
- Clear and step-by-step
- Easy to understand
- Focused on daily tasks
- Include examples when helpful""",
            
            "BUYER": """You are an AI assistant for Buyers in the Agri Export Management System.

You have access to:
- Product catalog and availability
- Your order history and status
- FAQ and ordering procedures
- Company policies

Your responses should be:
- Customer-friendly
- Help find products
- Explain the ordering process
- Provide clear pricing information"""
        }
        
        return prompts.get(role, "You are a helpful AI assistant for the Agri Export Management System.")
    
    def query(
        self, 
        query: str, 
        user_role: str,
        buyer_id: Optional[str] = None,
        buyer_status: Optional[str] = None
    ) -> Dict:
        """
        Execute role-based RAG query
        
        Args:
            query: User question
            user_role: User's role in system
            buyer_id: Buyer ID if role is BUYER
            buyer_status: Buyer status (PENDING/ACCEPTED) if role is BUYER
        
        Returns:
            Dict with 'answer' and 'sources'
        """
        try:
            # Build role-specific filter
            metadata_filter = self.build_metadata_filter(
                user_role, 
                buyer_status, 
                buyer_id
            )
            
            # Get role-specific system prompt
            system_prompt = self.get_role_system_prompt(user_role)
            
            # Create prompt template
            prompt_template = f"""{system_prompt}

Use the following context from the AEMS knowledge base to answer the question.
If the answer is not in the context, politely say you don't have that information.

Context:
{{context}}

Question: {{question}}

Helpful Answer:"""
            
            PROMPT = PromptTemplate(
                template=prompt_template,
                input_variables=["context", "question"]
            )
            
            # Create retriever with role-based filtering
            search_kwargs = {
                "k": settings.SEARCH_TOP_K
            }
            
            if metadata_filter:
                search_kwargs["filter"] = metadata_filter
            
            retriever = self.vector_store.as_retriever(
                search_kwargs=search_kwargs
            )
            
            # Create QA chain
            qa_chain = RetrievalQA.from_chain_type(
                llm=self.llm,
                chain_type="stuff",
                retriever=retriever,
                chain_type_kwargs={"prompt": PROMPT},
                return_source_documents=True
            )
            
            # Execute query
            result = qa_chain({"query": query})
            
            logger.info(
                f"RAG query executed for role {user_role}: '{query[:50]}...' "
                f"Found {len(result['source_documents'])} sources"
            )
            
            return {
                "answer": result["result"],
                "sources": [
                    {
                        "content": doc.page_content,
                        "metadata": doc.metadata
                    }
                    for doc in result["source_documents"]
                ],
                "role": user_role,
                "filter_applied": metadata_filter is not None
            }
            
        except Exception as e:
            logger.error(f"RAG query failed: {str(e)}")
            return {
                "answer": "I apologize, but I encountered an error processing your question. Please try again or contact support.",
                "sources": [],
                "error": str(e)
            }


# Singleton instance
rag_service = RAGService()
