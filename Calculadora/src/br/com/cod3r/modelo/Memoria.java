package br.com.cod3r.modelo;

import java.util.ArrayList;
import java.util.List;

public class Memoria {
	
	private enum TipoComando {
		ZERAR, NUMERO, SOMA, SUBTRACAO, MULTIPLICACAO, DIVISAO, VIRGULA, IGUAL
	}

	private static final Memoria instancia = new Memoria();
	private String textoAtual = "";
	private String textoBuffer = "";
	private boolean substituir = false;
	private TipoComando ultimaOperacao;
	private TipoComando ultimoComando;
	private final List<MemoriaObservador> observadores = new ArrayList<>();
	private double ultimoValor;
	
	private Memoria() {
	}

	public static Memoria getInstancia() {
		return instancia;
	}

	public String getTextoAtual() {
		return textoAtual.isEmpty() ? "0" : textoAtual;
	}
	
	public void processarComando (String texto) {
		TipoComando tipoComando = detectarTipoComando(texto);
		if (tipoComando == null) { 
			return;
		}	
		switch (tipoComando) {
		case ZERAR: 
			textoAtual = "";
			textoBuffer = "";
			substituir = false;
			ultimaOperacao = null;
			break;
		case NUMERO:
			textoAtual = substituir ? texto : textoAtual + texto;
			substituir = false;
			break;
		case SOMA:
		case SUBTRACAO:
		case MULTIPLICACAO:
		case DIVISAO:
			if (ultimoComando == TipoComando.NUMERO) {
				if (!textoBuffer.equals("")) {
					textoAtual = obterResultado();
					textoBuffer = textoAtual;
				}  
				textoBuffer = textoAtual;
				substituir = true;
				ultimaOperacao = tipoComando;
			} else if (ultimaOperacao != tipoComando) {
				ultimaOperacao = tipoComando;
			}
				break;
		case VIRGULA:
			if (textoAtual.equals("")) {
				textoAtual = "0,";
			} else {
				textoAtual = substituir ? texto : textoAtual + texto;
			}
			break;
		case IGUAL:
			if (ultimoComando == TipoComando.NUMERO) {
				textoAtual = obterResultado();
				textoBuffer = textoAtual;
				substituir = true;
			} else if (ultimoComando == TipoComando.IGUAL) {
				textoAtual = obterResultadoComUltimoValor();
				textoBuffer = textoAtual;
				substituir = true;
			}
			break;
		}
		ultimoComando = tipoComando;
		observadores.forEach(o -> o.alterarValor(getTextoAtual()));
	}
	
	private String obterResultado() {
		double intAtual = Double.parseDouble(textoAtual.replace(',', '.'));
		double intBuffer = Double.parseDouble(textoBuffer.replace(',', '.'));
		double resultado;
		ultimoValor = intAtual;
		if (ultimaOperacao==TipoComando.SOMA) {
			resultado = intBuffer + intAtual;
		} else if (ultimaOperacao==TipoComando.SUBTRACAO) {
			resultado = intBuffer - intAtual;
		} else if (ultimaOperacao==TipoComando.MULTIPLICACAO) {
			resultado = intBuffer * intAtual;
		} else {
			resultado = intBuffer / intAtual;
		}
		String retorno = Double.toString(resultado);
		retorno = retorno.replace('.', ',');
		if (retorno.contains(",0"))
			retorno = retorno.replace(",0", "");
		return retorno;
	}
	
	private String obterResultadoComUltimoValor() {
		double intBuffer = Double.parseDouble(textoBuffer.replace(',', '.'));
		double resultado;
		if (ultimaOperacao==TipoComando.SOMA) {
			resultado = intBuffer + ultimoValor;
		} else if (ultimaOperacao==TipoComando.SUBTRACAO) {
			resultado = intBuffer - ultimoValor;
		} else if (ultimaOperacao==TipoComando.MULTIPLICACAO) {
			resultado = intBuffer * ultimoValor;
		} else {
			resultado = intBuffer / ultimoValor;
		}
		String retorno = Double.toString(resultado);
		retorno = retorno.replace('.', ',');
		if (retorno.contains(",0"))
			retorno = retorno.replace(",0", "");
		return retorno;
	}

	private TipoComando detectarTipoComando(String texto) {
		if ((textoAtual.isEmpty() && texto.equals("0")) 
				|| (texto.equals(",") && textoAtual.contains(",")) 
				|| (texto.equals("=") && ultimaOperacao==null)) { 
			return null;
		} else if (texto.equals("AC")) {
			return TipoComando.ZERAR;
		} else if(texto.equals("+")) {
			return TipoComando.SOMA;
		} else if(texto.equals("-")) {
			return TipoComando.SUBTRACAO;
		} else if(texto.equals("x")) {
			return TipoComando.MULTIPLICACAO;
		} else if(texto.equals("/")) {
			return TipoComando.DIVISAO;
		} else if(texto.equals(",") && !textoAtual.contains("'")) {
			return TipoComando.VIRGULA;
		} else if(texto.equals("=") && ultimaOperacao!=null) {
			return TipoComando.IGUAL;
		} else {
			return TipoComando.NUMERO;
		}
	}

	public void adicionarObservadores(MemoriaObservador observador) {
		observadores.add(observador);
	}
}
