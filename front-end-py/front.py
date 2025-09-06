import streamlit as st
from supabase import create_client
import pandas as pd

# Pegue no Supabase: Project Settings > API
url = "https://mgjzcnaijbeyrkojudlz.supabase.co"
key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1nanpjbmFpamJleXJrb2p1ZGx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDk2NTIzNzMsImV4cCI6MjA2NTIyODM3M30.HVam7E4_Y6f4STRsDo_E0dYdJ_NXXIp5QMtB12PWWxw"  # chave anônima (pode usar no front)

supabase = create_client(url, key)

st.set_page_config(layout="wide") # Ajusta o layout para ocupar a largura total da tela
st.title("Indicadores Agregados Financeiros 🚀")
st.markdown("---")

# Dicionário de códigos BC com seus nomes
# ADICIONE AQUI OS CÓDIGOS E NOMES CORRESPONDENTES
codigo_nomes = {
    10813: "Dólar Americano",
    21619: "Cotação do Euro",
    4390:  "Taxa Selic",
    10844: "IPCA - Variação Mensal",
    13621: "Reservas Internacionais",
    13982: "Reservas Internacionais",
}

# --- Filtro para Anual ou Mensal ---
st.subheader("Filtro de Visualização")
tipo_visualizacao = st.radio(
    "Selecione o tipo de visualização:",
    ("Mensal", "Anual"),
    horizontal=True
)

st.markdown("---")

# --- Lógica de busca e exibição dos dados ---
try:
    if tipo_visualizacao == "Mensal":
        table_name = "indicador_agregado_mensal"
        date_format = "%Y-%m-%d"
        response = supabase.table(table_name).select("*").execute()
    else: # Anual
        table_name = "indicador_agregado_anual"
        date_format = "%Y"
        response = supabase.table(table_name).select("*").execute()

    if not response.data:
        st.warning("Nenhum dado encontrado para a visualização selecionada!")
    else:
        data = response.data
        df = pd.DataFrame(data)

        # Criar coluna de data unificada
        if tipo_visualizacao == "Mensal":
            df['data'] = pd.to_datetime(df['ano'].astype(str) + '-' + df['mes'].astype(str) + '-01')
        else:
            df['data'] = pd.to_datetime(df['ano'].astype(str), format="%Y")

        # Separar por codigo_bc
        codigos = df['codigo_bc'].unique()

        # --- Organizar gráficos lado a lado em colunas ---
        cols = st.columns(3)

        for i, codigo in enumerate(codigos):
            col = cols[i % 3] 
            with col:
                nome_codigo = codigo_nomes.get(codigo, f"Código BC: {codigo}")
                st.markdown(f"**{nome_codigo}**")
                
                df_cod = df[df['codigo_bc'] == codigo].sort_values('data')
                df_plot = df_cod[['data', 'valor_maximo', 'valor_medio', 'valor_minimo']]
                df_plot = df_plot.set_index('data')

                # Gráfico de linhas com todas as colunas de valor
                st.line_chart(df_plot)
                
                st.markdown("---")

except Exception as e:
    st.error(f"Ocorreu um erro: {e}")
