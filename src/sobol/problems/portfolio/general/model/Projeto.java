package sobol.problems.portfolio.general.model;

/**
 * Classe que representa um projeto candidato ao portf�lio
 * 
 * @author Marcio Barros
 */
public class Projeto
{
	private String nome;	// Nome do projeto
	private double custo;	// Custo necess�rio para implementar o projeto
	private double vpl;		// Valor Presente Liquido do projeto
	
	/**
	 * Constructor
	 */
	public Projeto(String nome, double vpl, double custo)
	{
		this.nome = nome;
		this.custo = -custo;
		this.vpl = vpl;
	}
	
	/**
	 * Retorna o nome do projeto
	 */
	public String getNome()
	{
		return this.nome;
	}

	/**
	 * Retorna o custo necess�rio para implementar o projeto
	 */
	public double getCusto()
	{
		return this.custo;
	}
		
	/**
	 * Retorna o Valor Presente L�quido do projeto
	 */
	public double getVPL()
	{
		return this.vpl;
	}
}