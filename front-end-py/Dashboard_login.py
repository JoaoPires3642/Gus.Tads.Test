import streamlit as st
from supabase import create_client

def login_page(go_to_register, go_to_dashboard):
    url = "https://mgjzcnaijbeyrkojudlz.supabase.co"
    key = (
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1nanpj"
        "bmFpamJleXJrb2p1ZGx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDk2NTIzNzMsImV4cCI6MjA2NTIyODM3M30.HVam7E4_"
        "Y6f4STRsDo_E0dYdJ_NXXIp5QMtB12PWWxw"
    )
    supabase = create_client(url, key)

    st.title("Login no Sistema")

    email = st.text_input("Email")
    senha = st.text_input("Senha", type="password")

    col1, col2 = st.columns(2)

    with col1:
        
      if st.button("Entrar"):
        try:
            
            response = supabase.table("users").select("*").eq("email", email).execute()
            data = response.data
            

            if not data:
                st.error("Usuário não encontrado")
                return  
            user = data[0]  # pega o primeiro usuário encontrado
            senha_cadastrada = user.get("senha")          
            
            if senha == senha_cadastrada:
                st.success(f"Login realizado com sucesso! Bem-vindo {user.get('nome')}")
                st.session_state["authenticated"] = True  # marca usuário como logado
                go_to_dashboard()
                st.rerun()
            else:
                st.error("Senha incorreta")

        except Exception as e:
            st.error(f"Erro ao tentar login: {e}")
        
        st.markdown("---")
        
    with col2:
      if st.button("Ainda não tem conta? Cadastrar-se"):
        go_to_register()
        st.rerun()
        