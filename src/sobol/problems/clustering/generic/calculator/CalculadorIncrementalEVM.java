package sobol.problems.clustering.generic.calculator;

import sobol.problems.clustering.generic.model.Project;
import sobol.problems.clustering.generic.model.ProjectClass;

/**
 * Classe que calcula o EVM de forma incremental para a otimiza��o baseada em um ponto
 * 
 * @author marcio.barros
 */
public class CalculadorIncrementalEVM
{
	/**
	 * N�mero de classes no problema
	 */
	private int classCount;
	
	/**
	 * N�mero de pacotes que ser�o considerados no problema
	 */
	private int packageCount;
	
	/**
	 * Matriz onde a c�lula [i, j] possui 1 se a classe I depende da classe J 
	 */
	private int[][] classEdges;

	/**
	 * Lista de classes que prestam servi�os para uma classe �ndice 
	 */
	private int[][] classDependsOn;

	/**
	 * Lista de classes que utilizam servi�os da classe �ndice
	 */
	private int[][] classProvidesTo;
	
	/**
	 * N�mero de arestas externas de cada pacote
	 */
	private int[] packageInterEdges;

	/**
	 * N�mero de arestas internas de cada pacote
	 */
	private int[] packageIntraEdges;
	
	/**
	 * Pacote em que � mantida cada classe
	 */
	private int[] newPackage;

	/**
	 * Inicializa o calculador incremental
	 */
	public CalculadorIncrementalEVM(Project project, int packageCount)
	{
		this.classCount = project.getClassCount();
		this.packageCount = packageCount;

		this.packageInterEdges = new int[packageCount];
		this.packageIntraEdges = new int[packageCount];
		
		prepareClassPackages(project);
		prepareClassDependencies(project);
		prepareClassProviders();
		prepareClassClients();
		preparePackageDependencies();
	}
	
	/**
	 * Monta o indicador do pacote ocupado por cada classe
	 */
	private void prepareClassPackages(Project project)
	{
		this.newPackage = new int[classCount];

		for (int i = 0; i < classCount; i++)
		{
			ProjectClass _class = project.getClassIndex(i);
			int sourcePackageIndex = project.getIndexForPackage(_class.getPackage());
			this.newPackage[i] = sourcePackageIndex;
		}
	}
	
	/**
	 * Monta a matriz de depend�ncias entre classes
	 */
	private void prepareClassDependencies(Project project)
	{
		this.classEdges = new int[classCount][classCount];
		
		for (int i = 0; i < classCount; i++)
		{
			ProjectClass _class = project.getClassIndex(i);
			
			for (int j = 0; j < _class.getDependencyCount(); j++)
			{
				String targetName = _class.getDependencyIndex(j).getElementName();
				int classIndex = project.getClassIndex(targetName);
				
				if (classIndex == -1)
					System.out.println("*** Class not registered in project: " + targetName);
				
				classEdges[i][classIndex] = 1;
			}
		}
	}

	/**
	 * Monta a lista de classes que oferecem servi�os para a classe �ndice
	 */
	private void prepareClassProviders()
	{
		this.classDependsOn = new int[classCount][classCount+1];
		
		for (int i = 0; i < classCount; i++)
		{
			int walker = 0;
			
			for (int j = 0; j < classCount; j++)
			{
				if (classEdges[i][j] > 0)
					classDependsOn[i][walker++] = j;
			}
			
			classDependsOn[i][walker] = -1;
		}
	}
	
	/**
	 * Monta a lista de classes que utilizam servi�os da classe �ndice
	 */
	private void prepareClassClients()
	{
		this.classProvidesTo = new int[classCount][classCount+1];
		int[] walkers = new int[classCount];
		
		for (int i = 0; i < classCount; i++)
		{
			for (int j = 0; j < classCount; j++)
			{
				if (classEdges[i][j] > 0)
					classProvidesTo[j][walkers[j]++] = i;
			}
		}
		
		for (int i = 0; i < classCount; i++)
			classProvidesTo[i][walkers[i]] = -1;
	}
	
	/**
	 * Calcula o n�mero de arestas internas e externas de cada pacote
	 */
	private void preparePackageDependencies()
	{
		for (int i = 0; i < packageCount; i++)
			this.packageInterEdges[i] = this.packageIntraEdges[i] = 0;

		for (int i = 0; i < classCount; i++)
		{
			int sourcePackage = newPackage[i];
			int classIndex;
			
			for (int j = 0; (classIndex = classDependsOn[i][j]) >= 0; j++)
			{
				int targetPackage = newPackage[classIndex];
				
				if (targetPackage != sourcePackage)
				{
					packageInterEdges[sourcePackage]++;
					packageInterEdges[targetPackage]++;
				}
				else
					packageIntraEdges[sourcePackage]++;
			}
		}
	}
	
	/**
	 * Contabiliza a influ�ncia de uma classe nas arestas de cada pacote
	 */
	private void addClassInfluence(int classIndex)
	{
		int sourcePackage = newPackage[classIndex];
		int secondClassIndex;
		
		for (int i = 0; (secondClassIndex = classDependsOn[classIndex][i]) >= 0; i++)
		{
			int targetPackage = newPackage[secondClassIndex];
			
			if (targetPackage != sourcePackage)
			{
				packageInterEdges[sourcePackage]++;
				packageInterEdges[targetPackage]++;
			}
			else
				packageIntraEdges[sourcePackage]++;
		}
		
		for (int i = 0; (secondClassIndex = classProvidesTo[classIndex][i]) >= 0; i++)
		{
			int targetPackage = newPackage[secondClassIndex];
			
			if (targetPackage != sourcePackage)
			{
				packageInterEdges[sourcePackage]++;
				packageInterEdges[targetPackage]++;
			}
			else
				packageIntraEdges[sourcePackage]++;
		}
	}
	
	/**
	 * Descontabiliza a influ�ncia de uma classe nas arestas de cada pacote
	 */
	public void removeClassInfluence(int classIndex)
	{
		int sourcePackage = newPackage[classIndex];
		int secondClassIndex;
		
		for (int i = 0; (secondClassIndex = classDependsOn[classIndex][i]) >= 0; i++)
		{
			int targetPackage = newPackage[secondClassIndex];

			if (targetPackage != sourcePackage)
			{
				packageInterEdges[sourcePackage]--;
				packageInterEdges[targetPackage]--;
			}
			else
				packageIntraEdges[sourcePackage]--;
		}
		
		for (int i = 0; (secondClassIndex = classProvidesTo[classIndex][i]) >= 0; i++)
		{
			int targetPackage = newPackage[secondClassIndex];

			if (targetPackage != sourcePackage)
			{
				packageInterEdges[sourcePackage]--;
				packageInterEdges[targetPackage]--;
			}
			else
				packageIntraEdges[sourcePackage]--;
		}
	}
	
	/**
	 * Move uma classe para outro pacote
	 */
	public void moveClass(int classIndex, int packageIndex)
	{
		int actualPackage = newPackage[classIndex];
		
		if (actualPackage != packageIndex)
		{
			removeClassInfluence(classIndex);
			newPackage[classIndex] = packageIndex;
			addClassInfluence(classIndex);
		}
	}
	
	/**
	 * Move todas as classes para um novo conjunto de pacotes
	 */
	public void moveAll(int[] packageIndexes)
	{
		for (int i = 0; i < classCount; i++)
			newPackage[i] = packageIndexes[i];
		
		preparePackageDependencies();
	}
	
	/**
	 * Calcula o valor do EVM para a distribui��o atual de classes em pacotes
	 */
	public double calculateEVM()
	{
		double result = 0.0;
		
		for (int i = 0; i < packageCount; i++)
		{
			// TODO implementar - tv seja �til guardar o n�mero de classes por pacote, acho que n�o precisa de interedges
			result += 0.0;
		}
		
		return result;
	}
}