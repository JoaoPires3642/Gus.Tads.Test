import streamlit as st
from supabase import create_client, Client
import pandas as pd
import plotly.graph_objects as go
import plotly.express as px
from plotly.subplots import make_subplots
from streamlit_echarts import st_echarts
import numpy as np
from datetime import datetime, timedelta
import warnings
warnings.filterwarnings('ignore')

def dashboard_page():

 st.set_page_config(
    layout="wide", 
    page_title="Dashboard Macroeconômico Brasil",
    page_icon="🇧🇷",
    initial_sidebar_state="expanded"
)


st.markdown("""
<style>
    .main-header {
        background: linear-gradient(90deg, #1e3c72 0%, #2a5298 100%);
        padding: 2rem;
        border-radius: 10px;
        margin-bottom: 2rem;
        color: white;
        text-align: center;
    }
    
    .metric-card {
        background: white;
        padding: 1.5rem;
        border-radius: 10px;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        border-left: 4px solid #2a5298;
        margin-bottom: 1rem;
    }
    
    .kpi-positive {
        color: #28a745;
        font-weight: bold;
    }
    
    .kpi-negative {
        color: #dc3545;
        font-weight: bold;
    }
    
    .sidebar-header {
        background: #f8f9fa;
        padding: 1rem;
        border-radius: 5px;
        margin-bottom: 1rem;
    }
    
    .info-box {
        background: #e3f2fd;
        padding: 1rem;
        border-radius: 5px;
        border-left: 4px solid #2196f3;
        margin: 1rem 0;
    }
</style>
""", unsafe_allow_html=True)

# --- Conexão com Supabase ---
@st.cache_resource
def init_connection():
    url = "https://mgjzcnaijbeyrkojudlz.supabase.co"
    key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im1nanpj" \
    "bmFpamJleXJrb2p1ZGx6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDk2NTIzNzMsImV4cCI6MjA2NTIyODM3M30.HVam7E4_" \
    "Y6f4STRsDo_E0dYdJ_NXXIp5QMtB12PWWxw"
    
    if not url or not key:
        st.error("❌ Credenciais do Supabase não encontradas.")
        return None
    return create_client(url, key)

supabase = init_connection()

if not supabase:
    st.stop()

# --- Dicionário de Indicadores ---
INDICADORES = {
    10813: {"nome": "Dólar Americano (Venda)", "unidade": "R$", "categoria": "Câmbio e Reservas", "icon": "💵"},
    21619: {"nome": "Euro", "unidade": "R$", "categoria": "Câmbio e Reservas", "icon": "💶"},
    13982: {"nome": "Reservas Internacionais", "unidade": "US$ Mi", "categoria": "Câmbio e Reservas", "icon": "🏦"},
    4390: {"nome": "Taxa Selic", "unidade": "%", "categoria": "Juros e Inflação", "icon": "📈"},
    10844: {"nome": "IPCA", "unidade": "%", "categoria": "Juros e Inflação", "icon": "📊"},
    24369: {"nome": "IBC-Br (Prévia do PIB)", "unidade": "Índice", "categoria": "Atividade Econômica", "icon": "🏗️"},
    28763: {"nome": "CAGED", "unidade": "Vagas", "categoria": "Atividade Econômica", "icon": "🧑‍💼"},
    4513: {"nome": "Dívida Líquida", "unidade": "% PIB", "categoria": "Fiscal", "icon": "🧾"}
}

# --- Funções de Busca de Dados ---
@st.cache_data(ttl=3600)
def fetch_data(table_name: str) -> pd.DataFrame:
    """Busca e processa dados de uma tabela específica."""
    try:
        with st.spinner(f"Carregando dados de {table_name}..."):
            response = supabase.table(table_name).select("*").execute()
            
        if not response.data:
            return pd.DataFrame()
        
        df = pd.DataFrame(response.data)
        df['codigo_bc'] = df['codigo_bc'].astype(int)
        
        if 'ano' in df.columns:
            df['ano'] = df['ano'].astype(int)
        
        if 'mes' in df.columns:
            df['mes'] = df['mes'].astype(int)
            df['data'] = pd.to_datetime(df['ano'].astype(str) + '-' + df['mes'].astype(str) + '-01')
        else:
            df['data'] = pd.to_datetime(df['ano'].astype(str) + '-01-01')
            
        return df.sort_values('data')
    except Exception as e:
        st.error(f"❌ Erro ao buscar dados da tabela {table_name}: {e}")
        return pd.DataFrame()

@st.cache_data(ttl=600)
def fetch_latest_kpis() -> dict:
    """Busca o valor mais recente de cada indicador."""
    try:
        response = supabase.rpc('get_latest_indicators').execute()
        if not response.data:
            return {}
        return {item['codigo_bc']: item['valor'] for item in response.data}
    except Exception as e:
        st.warning(f"⚠️ Erro ao buscar KPIs: {e}")
        return {}

def calculate_variation(df: pd.DataFrame, periods: int = 1) -> tuple:
    if len(df) < periods + 1:
        return 0, "(dados insuficientes)"
    current = df.iloc[-1]['valor_medio']
    previous = df.iloc[-(periods + 1)]['valor_medio']
    var = ((current - previous) / previous) * 100 if previous != 0 else 0
    return var, ""

# --- Header Principal ---
def render_header():
    st.markdown("""
    <div class="main-header">
        <h1>🇧🇷 Dashboard Macroeconômico do Brasil</h1>
        <p>Acompanhe os principais indicadores econômicos</p>
    </div>
    """, unsafe_allow_html=True)


def render_dashboard():
    render_header()
    
   
    st.markdown("## 📊 Indicadores Principais")
    
    latest_kpis = fetch_latest_kpis()
    df_mensal = fetch_data("indicador_agregado_mensal")
    
    cols = st.columns(min(len(INDICADORES), 4))  
    
    for i, (code, info) in enumerate(INDICADORES.items()):
        with cols[i % len(cols)]:
            value = latest_kpis.get(code, "N/A")
            
            
            variation = 0
            if not df_mensal.empty and code in df_mensal['codigo_bc'].values:
                df_indicator = df_mensal[df_mensal['codigo_bc'] == code].tail(2)
                variation, _ = calculate_variation(df_indicator)
            
            
            if isinstance(value, (int, float)):
                if info['unidade'] == '%':
                    formatted_value = f"{value:.2f}%"
                elif info['unidade'] == 'R$':
                    formatted_value = f"R$ {value:.2f}"
                else:
                    formatted_value = f"{value:,.0f}"
            else:
                formatted_value = str(value)
            
            
            delta_color = "normal"
            if variation > 0:
                delta_color = "inverse" if code in [10813, 21619, 4390, 10844] else "normal"
            elif variation < 0:
                delta_color = "normal" if code in [10813, 21619, 4390, 10844] else "inverse"
            
            st.metric(
                label=f"{info['icon']} {info['nome']}", 
                value=formatted_value,
                delta=f"{variation:+.2f}%" if variation != 0 else None,
                delta_color=delta_color
            )
    
    
    #Grafico
    st.markdown("## 📈 Evolução Temporal")
    
    if not df_mensal.empty:
        categorias = list(set(info['categoria'] for info in INDICADORES.values()))
        for cat in categorias:
            st.markdown(f"### {cat}")
            
            codes_cat = [code for code in INDICADORES if INDICADORES[code]['categoria'] == cat]
            
            if len(codes_cat) == 0:
                continue
            
            # Lógica para gráficos que fazem sentido Estarem juntos
            if cat == "Câmbio e Reservas":
                
                fig_cambio = go.Figure()
                for code in [10813, 21619]:
                    if code in df_mensal['codigo_bc'].values:
                        df_indicator = df_mensal[df_mensal['codigo_bc'] == code].tail(12)
                        fig_cambio.add_trace(go.Scatter(
                            x=df_indicator['data'],
                            y=df_indicator['valor_medio'],
                            mode='lines+markers',
                            name=INDICADORES[code]['nome'],
                            line=dict(width=3)
                        ))
                fig_cambio.update_layout(
                    height=400,
                    showlegend=True,
                    xaxis_title="Período",
                    yaxis_title="Valor (R$)",
                    template="plotly_white"
                )
                st.plotly_chart(fig_cambio, use_container_width=True, key=f"chart_cambio_{cat.replace(' ', '_')}")
                
                # Reservas 
                if 13982 in codes_cat and 13982 in df_mensal['codigo_bc'].values:
                    fig_reservas = go.Figure()
                    df_reservas = df_mensal[df_mensal['codigo_bc'] == 13982].tail(12)
                    fig_reservas.add_trace(go.Scatter(
                        x=df_reservas['data'],
                        y=df_reservas['valor_medio'],
                        mode='lines+markers',
                        name=INDICADORES[13982]['nome'],
                        line=dict(width=3)
                    ))
                    fig_reservas.update_layout(
                        height=400,
                        showlegend=True,
                        xaxis_title="Período",
                        yaxis_title="US$ Mi",
                        template="plotly_white"
                    )
                    st.plotly_chart(fig_reservas, use_container_width=True, key=f"chart_reservas_{cat.replace(' ', '_')}")
            
            elif cat == "Juros e Inflação":
                # Selic e IPCA 
                fig_econ = make_subplots(specs=[[{"secondary_y": True}]])
                if 10844 in df_mensal['codigo_bc'].values:
                    df_ipca = df_mensal[df_mensal['codigo_bc'] == 10844].tail(12)
                    fig_econ.add_trace(
                        go.Scatter(x=df_ipca['data'], y=df_ipca['valor_medio'], 
                                  name="IPCA (%)", line=dict(color='red', width=3)),
                        secondary_y=False
                    )
                if 4390 in df_mensal['codigo_bc'].values:
                    df_selic = df_mensal[df_mensal['codigo_bc'] == 4390].tail(12)
                    fig_econ.add_trace(
                        go.Scatter(x=df_selic['data'], y=df_selic['valor_medio'], 
                                  name="Selic (%)", line=dict(color='blue', width=3)),
                        secondary_y=True
                    )
                fig_econ.update_yaxes(title_text="IPCA (%)", secondary_y=False)
                fig_econ.update_yaxes(title_text="Selic (%)", secondary_y=True)
                fig_econ.update_layout(height=400, template="plotly_white")
                st.plotly_chart(fig_econ, use_container_width=True, key=f"chart_juros_inflacao_{cat.replace(' ', '_')}")
            
            elif cat == "Atividade Econômica":
                # IBC-Br e CAGED 
                fig_ativ = go.Figure()
                for code in codes_cat:
                    if code in df_mensal['codigo_bc'].values:
                        df_indicator = df_mensal[df_mensal['codigo_bc'] == code].tail(12)
                        fig_ativ.add_trace(go.Scatter(
                            x=df_indicator['data'],
                            y=df_indicator['valor_medio'],
                            mode='lines+markers',
                            name=INDICADORES[code]['nome'],
                            line=dict(width=3)
                        ))
                fig_ativ.update_layout(
                    height=400,
                    showlegend=True,
                    xaxis_title="Período",
                    yaxis_title="Valor",
                    template="plotly_white"
                )
                st.plotly_chart(fig_ativ, use_container_width=True, key=f"chart_atividade_{cat.replace(' ', '_')}")
            
            else:
                # Categorias com um indicador 
                fig_cat = go.Figure()
                for code in codes_cat:
                    if code in df_mensal['codigo_bc'].values:
                        df_indicator = df_mensal[df_mensal['codigo_bc'] == code].tail(12)
                        fig_cat.add_trace(go.Scatter(
                            x=df_indicator['data'],
                            y=df_indicator['valor_medio'],
                            mode='lines+markers',
                            name=INDICADORES[code]['nome'],
                            line=dict(width=3)
                        ))
                fig_cat.update_layout(
                    height=400,
                    showlegend=True,
                    xaxis_title="Período",
                    yaxis_title="Valor",
                    template="plotly_white"
                )
                st.plotly_chart(fig_cat, use_container_width=True, key=f"chart_{cat.replace(' ', '_')}")
    

# --- Página: Análise Avançada ---
def render_analise_avancada():
    st.markdown("# 🔬 Análise Avançada")
    
    granularidade = st.sidebar.selectbox(
        "📅 Granularidade dos Dados",
        ["Mensal", "Anual"],
        help="Escolha a frequência dos dados para análise"
    )
    
    # Período de análise (adaptado)
    if granularidade == "Mensal":
        label_periodo = "📊 Período de Análise (meses)"
        min_val, max_val, default_val = 1, 120, 24 
        offset_type = 'months'
    else:
        label_periodo = "📊 Período de Análise (anos)"
        min_val, max_val, default_val = 1, 10, 5
        offset_type = 'years'
    
    periodo = st.sidebar.slider(
        label_periodo,
        min_value=min_val, max_value=max_val, value=default_val,
        help="Selecione o período para incluir na análise"
    )
    
    table_name = f"indicador_agregado_{granularidade.lower()}"
    df_dados = fetch_data(table_name)
    
    if df_dados.empty:
        st.warning(f"⚠️ Nenhum dado {granularidade.lower()} encontrado.")
        return
    

    indicadores_disponiveis = {
        code: info['nome'] for code, info in INDICADORES.items() 
        if code in df_dados['codigo_bc'].unique()
    }
    
    indicadores_selecionados = st.sidebar.multiselect(
        "📈 Indicadores para Análise",
        options=list(indicadores_disponiveis.values()),
        default=list(indicadores_disponiveis.values())[:3],
        help="Selecione os indicadores que deseja analisar"
    )
    
    if not indicadores_selecionados:
        st.info("ℹ️ Selecione pelo menos um indicador para continuar.")
        return
    

    codigos_selecionados = [
        code for code, nome in indicadores_disponiveis.items() 
        if nome in indicadores_selecionados
    ]
    

    max_period_var = 12 if granularidade == "Mensal" else 5
    adjusted_period = periodo + max_period_var
    offset_dict = {offset_type: adjusted_period}
    df_filtrado = df_dados[
        (df_dados['codigo_bc'].isin(codigos_selecionados)) &
        (df_dados['data'] >= df_dados['data'].max() - pd.DateOffset(**offset_dict))
    ].sort_values('data')
    

    vis_offset_dict = {offset_type: periodo}
    df_vis = df_filtrado[
        df_filtrado['data'] >= df_filtrado['data'].max() - pd.DateOffset(**vis_offset_dict)
    ]
    
    # Tabs
    tab1, tab2, tab3, tab4 = st.tabs(["📊 Séries Temporais", "📈 Tendências", "📋 Estatísticas", "🔄 Comparativo"])
    
    with tab1:
        st.markdown("### 📊 Evolução das Séries Temporais")
        
        fig_series = go.Figure()
        
        for code in codigos_selecionados:
            df_indicator = df_vis[df_vis['codigo_bc'] == code]
            nome = indicadores_disponiveis[code]
            
            fig_series.add_trace(go.Scatter(
                x=df_indicator['data'],
                y=df_indicator['valor_medio'],
                mode='lines+markers',
                name=nome,
                line=dict(width=3),
                hovertemplate=f"<b>{nome}</b><br>Data: %{{x}}<br>Valor: %{{y:.2f}}<extra></extra>"
            ))
        
        fig_series.update_layout(
            height=500,
            xaxis_title="Período",
            yaxis_title="Valor",
            template="plotly_white",
            hovermode='x unified'
        )
        
        st.plotly_chart(fig_series, use_container_width=True, key="chart_series_temporais")
    
    with tab2:
        st.markdown("### 📈 Análise de Tendências")
        
        col1, col2 = st.columns(2)
        
        with col1:
            st.markdown("#### 📊 Variações Recentes")
            
            if granularidade == "Mensal":
                periods_list = [1, 3, 12]
                labels_list = ["1 mês", "3 meses", "12 meses"]
            else:
                periods_list = [1, 3, 5]
                labels_list = ["1 ano", "3 anos", "5 anos"]
            
            for code in codigos_selecionados:
                df_indicator = df_filtrado[df_filtrado['codigo_bc'] == code]
                if len(df_indicator) >= 2:
                    nome = indicadores_disponiveis[code]
                    var_short, msg_short = calculate_variation(df_indicator, periods_list[0])
                    var_med, msg_med = calculate_variation(df_indicator, periods_list[1])
                    var_long, msg_long = calculate_variation(df_indicator, periods_list[2])
                    
                    st.markdown(f"**{INDICADORES[code]['icon']} {nome}**")
                    col_a, col_b, col_c = st.columns(3)
                    with col_a:
                        st.metric(labels_list[0], f"{var_short:+.2f}% {msg_short}")
                    with col_b:
                        st.metric(labels_list[1], f"{var_med:+.2f}% {msg_med}")
                    with col_c:
                        st.metric(labels_list[2], f"{var_long:+.2f}% {msg_long}")
        
        with col2:
            st.markdown("#### 📊 Análise de Volatilidade")
            
            volatilidade_data = []
            for code in codigos_selecionados:
                df_indicator = df_vis[df_vis['codigo_bc'] == code]
                if len(df_indicator) > 1:
                    nome = indicadores_disponiveis[code]
                    volatilidade = df_indicator['valor_medio'].std()
                    volatilidade_data.append({'Indicador': nome, 'Volatilidade': volatilidade})
            
            if volatilidade_data:
                df_vol = pd.DataFrame(volatilidade_data)
                fig_vol = px.bar(
                    df_vol, x='Indicador', y='Volatilidade',
                    title="Volatilidade dos Indicadores",
                    template="plotly_white"
                )
                st.plotly_chart(fig_vol, use_container_width=True, key="chart_volatilidade")
    
    with tab3:
        st.markdown("### 📋 Estatísticas Descritivas")
        
        stats_data = []
        for code in codigos_selecionados:
            df_indicator = df_vis[df_vis['codigo_bc'] == code]
            if not df_indicator.empty:
                nome = indicadores_disponiveis[code]
                stats = {
                    'Indicador': nome,
                    'Média': df_indicator['valor_medio'].mean(),
                    'Mediana': df_indicator['valor_medio'].median(),
                    'Desvio Padrão': df_indicator['valor_medio'].std(),
                    'Mínimo': df_indicator['valor_medio'].min(),
                    'Máximo': df_indicator['valor_medio'].max(),
                    'Último Valor': df_indicator['valor_medio'].iloc[-1]
                }
                stats_data.append(stats)
        
        if stats_data:
            df_stats = pd.DataFrame(stats_data)
            st.dataframe(df_stats.round(4), use_container_width=True)
    
    with tab4:
        st.markdown("### 🔄 Análise Comparativa")
        
        if len(codigos_selecionados) >= 2:
            fig_norm = go.Figure()
            
            for code in codigos_selecionados:
                df_indicator = df_vis[df_vis['codigo_bc'] == code]
                if not df_indicator.empty:
                    nome = indicadores_disponiveis[code]
                    valores_norm = (df_indicator['valor_medio'] / df_indicator['valor_medio'].iloc[0]) * 100
                    
                    fig_norm.add_trace(go.Scatter(
                        x=df_indicator['data'],
                        y=valores_norm,
                        mode='lines+markers',
                        name=f"{nome} (Base 100)",
                        line=dict(width=3)
                    ))
            
            fig_norm.update_layout(
                height=500,
                title="Comparação Normalizada (Base 100 = Primeiro Valor)",
                xaxis_title="Período",
                yaxis_title="Índice (Base 100)",
                template="plotly_white"
            )
            
            st.plotly_chart(fig_norm, use_container_width=True, key="chart_comparativa")
        else:
            st.info("ℹ️ Selecione pelo menos 2 indicadores para análise comparativa.")



def main():
    st.sidebar.markdown("""
    <div style="text-align: center; padding: 1rem;">
        <h2>🇧🇷 Dashboard Macro</h2>
        <p>Indicadores Econômicos do Brasil</p>
    </div>
    """, unsafe_allow_html=True)
    
    pages = {
        "🏠 Dashboard": render_dashboard,
        "🔬 Análise Avançada": render_analise_avancada
    }
    
    selected_page = st.sidebar.radio("📍 Navegação", list(pages.keys()))
    
    st.sidebar.markdown("---")
    pages[selected_page]()


  