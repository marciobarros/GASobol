package sobol.problems.portfolio.general.reader;

import java.util.Hashtable;
import sobol.experiments.instancegen.FlatPublisherReader;
import sobol.experiments.instancegen.FlatPublisherReaderException;
import sobol.problems.portfolio.general.model.Projeto;
import sobol.problems.portfolio.general.model.ProjetosCandidatos;
import sobol.problems.portfolio.general.model.Risco;

/**
 * Classe que faz a leitura da inst�ncia contida no arquivo texto gerado 
 * pelo InstanceGen para dentro dos atributos da classe ProjetosCandidatos 
 */
public class PortfolioReader extends FlatPublisherReader 
{
	private ProjetosCandidatos candidatos;

	/**
	 * Constructor
	 */
	public PortfolioReader()
	{
		this.candidatos = new ProjetosCandidatos();
	}
	
	/**
	 * M�todo que retorna todos os projetos candidatos lidos da inst�ncia
	 */
	public ProjetosCandidatos getCandidatos()
	{
		return candidatos;
	}
	
	/**
	 * M�todo que grava os dados do arquivo texto para dentro dos atributos 
	 * da classe que representa um projeto candidato
	 * 
	 * @param type par�metro que indica o tipo de entidade (projeto, risco, ...)
	 * @param name par�metro que indica o nome da entidade (descri��o)
	 * @param attributes Lista de campos ou atributos da entidade
	 */
	@Override
	protected void registerEntity(String type, String name, Hashtable<String, String> attributes) throws FlatPublisherReaderException 
	{
		if (type.compareToIgnoreCase("project") == 0)
		{
			double vpl = Double.parseDouble(attributes.get("vpl"));
			double custo = Double.parseDouble(attributes.get("cost"));
			Projeto projeto = new Projeto(name, vpl, custo);
			candidatos.adicionaProjeto(projeto);
			return;
		}
		
		if (type.compareToIgnoreCase("risk") == 0)
		{
			double probabilidade = Double.parseDouble(attributes.get("probability"));
			Risco risco = new Risco(name, probabilidade);
			candidatos.adicionaRisco(risco);
			return;
		}
		
		generateException("entidade desconhecida '" + type + "'");
	}

	/**
	 * M�todo que grava os dados do relacionamento entre determinado risco com 
	 * determinado projeto
	 * 
	 * @param type par�metro que indica o tipo de entidade (projeto, risco, ...)
	 * @param source par�metro que indica o nome da entidade (descri��o) que afeta a entidade target
	 * @param target par�metro que indica o nome da entidade (descri��o) que est� sendo afetada pela entidade source
	 * @param attributes Lista de campos ou atributos da entidade
 	 */
	@Override
	protected void registerRelationship(String type, String source, String target, Hashtable<String, String> attributes) throws FlatPublisherReaderException 
	{
		if (type.compareToIgnoreCase("affects") == 0)
		{
			double impacto = Double.parseDouble(attributes.get("impact"));

			Risco risco = candidatos.pegaRiscoNome(source);
			
			if (risco == null)
				generateException("unknown risk '" + source + "'");
			
			Projeto projeto = candidatos.pegaProjetoNome(target);
			
			if (projeto == null)
				generateException("Projeto desconhecido '" + target + "'");

			risco.adicionaEfeito(projeto, impacto);
			return;
		}
		
		generateException("entidade desconhecida '" + type + "'");
	}
}