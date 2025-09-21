import streamlit as st
from Dashboard_login import login_page
from Dashboard_cadastro import register_page
import Dashboard_graphics as dg

# --- Estado da sessão ---
if "page" not in st.session_state:
    st.session_state["page"] = "login"  # Pode ser: login, register, dashboard
if "authenticated" not in st.session_state:
    st.session_state["authenticated"] = False

# --- Funções de navegação ---
def go_to_login():
    st.session_state["page"] = "login"

def go_to_register():
    st.session_state["page"] = "register"

def go_to_dashboard():
    st.session_state["page"] = "dashboard"

# --- Controladora ---
def main_controller():
    
    if st.session_state["page"] == "login":
        login_page(go_to_register, go_to_dashboard)
        
    elif st.session_state["page"] == "register":
        register_page(go_to_login, go_to_dashboard)
        
    elif st.session_state["page"] == "dashboard":
        dg.main()
        

if __name__ == "__main__":
    main_controller()
