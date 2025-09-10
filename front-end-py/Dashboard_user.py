import streamlit as st
import requests
from supabase import create_client
import pandas as pd

# --- Configuração ---
JAVA_API_URL = "http://localhost:8080/api/users"
SUPABASE_URL = "https://mgjzcnaijbeyrkojudlz.supabase.co"
SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1nanpj" \
    "bmFpamJleXJrb2p1ZGx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDk2NTIzNzMsImV4cCI6MjA2NTIyODM3M30.HVam7E4_" \
    "Y6f4STRsDo_E0dYdJ_NXXIp5QMtB12PWWxw"

st.set_page_config(
    layout="wide",
    page_title="Dashboard Macroeconômico Brasil",
    page_icon="🇧🇷"
)

# --- Conexão com Supabase ---
@st.cache_resource
def init_supabase():
    return create_client(SUPABASE_URL, SUPABASE_KEY)

supabase = init_supabase()

# --- Estado da Sessão ---
if "authenticated" not in st.session_state:
    st.session_state["authenticated"] = False
if "show_register" not in st.session_state:
    st.session_state["show_register"] = False


# --- Login ---
def login_page():
    st.title("🔒 Login")

    with st.form("login_form"):
        email = st.text_input("E-mail")
        password = st.text_input("Senha", type="password")
        submitted_login = st.form_submit_button("Entrar")

        if submitted_login:
            try:
                resp = requests.post(f"{JAVA_API_URL}/login", json={"email": email, "senha": password})
                if resp.status_code == 200:
                    st.session_state["authenticated"] = True
                    st.success("✅ Login realizado com sucesso!")
                    st.rerun()
                else:
                    st.error("❌ Usuário ou senha inválidos.")
            except Exception:
                st.error("Erro ao conectar ao backend Java.")


    st.button("Não tem conta? Criar", on_click=lambda: st.session_state.update(show_register=True))

    if st.session_state["show_register"]:
        with st.form("register_form"):
            nome = st.text_input("Nome")
            email_reg = st.text_input("E-mail")
            senha_reg = st.text_input("Senha", type="password")
            nascimento = st.date_input("Data de nascimento")
            submitted_reg = st.form_submit_button("Cadastrar")

            if submitted_reg:
                try:
                    resp = requests.post(f"{JAVA_API_URL}/register", json={
                        "nome": nome,
                        "email": email_reg,
                        "senha": senha_reg,
                        "dataNascimento": str(nascimento)
                    })
                    if resp.status_code == 200:
                        st.success("🎉 Conta criada, faça login!")
                        st.session_state["show_register"] = False
                    else:
                        st.error("Erro ao cadastrar. Tente outro e-mail.")
                except Exception:
                    st.error("Erro ao conectar ao backend Java.")


# --- Dashboard com dados do Supabase ---
def dashboard_page():
    st.title("📊 Dashboard Macroeconômico Brasil")

    # Exemplo de consulta ao Supabase
    response = supabase.table("indicador_agregado_mensal").select("*").execute()
    if not response.data:
        st.warning("⚠️ Nenhum dado encontrado no Supabase.")
        return

    df = pd.DataFrame(response.data)
    st.dataframe(df.head())  # só pra testar

    # Aqui você pode colar suas funções de renderização dos gráficos (render_dashboard, render_analise_avancada, etc.)


# --- Main ---
def main():
    if not st.session_state["authenticated"]:
        login_page()
    else:
        dashboard_page()

if __name__ == "__main__":
    main()
