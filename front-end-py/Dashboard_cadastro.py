import requests
import streamlit as st

def register_page(go_to_login, go_to_dashboard):
    JAVA_API_URL = "http://localhost:8080/api/users"

    st.title("Cadastro")

    nome = st.text_input("Nome")
    email = st.text_input("Email")
    senha = st.text_input("Senha", type="password")
    data_nascimento = st.date_input("Data de Nascimento")

    if st.button("Cadastrar"):
        payload = {
            "nome": nome,
            "email": email,
            "senha": senha,
            "dataNascimento": str(data_nascimento)
        }

        response = requests.post(JAVA_API_URL, json=payload)

        if response.status_code in (200, 201):
            st.success("Usuário cadastrado com sucesso via Service (Java + Supabase)!")
        else:
      
           st.error(f"Erro: {response.text}")
           
    st.markdown("---")
    if st.button("Voltar para Login"):
      go_to_login()