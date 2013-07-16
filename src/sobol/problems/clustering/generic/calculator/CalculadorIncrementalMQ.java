package sobol.problems.clustering.generic.calculator;

import sobol.problems.clustering.generic.model.Project;
import sobol.problems.clustering.generic.model.ProjectClass;

public class CalculadorIncrementalMQ
{
	private int classCount;
	private int packageCount;
	private int[][] classEdges;
	private int[] packageInterEdges;
	private int[] packageIntraEdges;
	private int[] originalPackage;
	private int[] newPackage;
	private double[] mq;

	public CalculadorIncrementalMQ(Project project, int packageCount)
	{
		this.classCount = project.getClassCount();
		this.packageCount = packageCount;

		this.packageInterEdges = new int[packageCount];
		this.packageIntraEdges = new int[packageCount];
		
		preparePackages(project);
		prepareClassDependencies(project);

		this.mq = new double[packageCount];
		prepareMQ();
	}
	
	private void preparePackages(Project project)
	{
		this.originalPackage = new int[classCount];
		this.newPackage = new int[classCount];

		for (int i = 0; i < classCount; i++)
		{
			ProjectClass _class = project.getClassIndex(i);
			int sourcePackageIndex = project.getIndexForPackage(_class.getPackage());
			this.originalPackage[i] = this.newPackage[i] = sourcePackageIndex;
		}
	}
	
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
	
	private void preparePackageDependencies()
	{
		for (int i = 0; i < packageCount; i++)
		{
			this.packageInterEdges[i] = 0;
			this.packageIntraEdges[i] = 0;
		}

		for (int i = 0; i < classCount; i++)
			addClassInfluence(i);
	}
	
	private void addClassInfluence(int classIndex)
	{
		int sourcePackage = newPackage[classIndex];
		
		for (int i = 0; i < classCount; i++)
		{
			if (classEdges[classIndex][i] > 0)
			{
				int targetPackage = newPackage[i];
				
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
	
	private void removeClassInfluence(int classIndex)
	{
		int sourcePackage = newPackage[classIndex];
		
		for (int i = 0; i < classCount; i++)
		{
			int targetPackage = newPackage[i];

			if (classEdges[classIndex][i] > 0)
			{				
				if (targetPackage != sourcePackage)
				{
					packageInterEdges[sourcePackage]--;
					packageInterEdges[targetPackage]--;
				}
				else
					packageIntraEdges[sourcePackage]--;
			}

			if (classEdges[i][classIndex] > 0)
			{
				if (targetPackage != sourcePackage)
				{
					packageInterEdges[sourcePackage]--;
					packageInterEdges[targetPackage]--;
				}
				else
					packageIntraEdges[sourcePackage]--;
			}
		}
	}
	
	private void prepareMQ()
	{
		preparePackageDependencies();

		for (int i = 0; i < packageCount; i++)
			mq[i] = calculateModularizationFactor(i);
	}
	
	private double calculateModularizationFactor(int packageIndex)
	{
		int inter = packageInterEdges[packageIndex];
		int intra = packageIntraEdges[packageIndex];
		
		if (intra != 0 && inter != 0)
			return intra / (intra + 0.5 * inter);

		return 0.0;
	}

	public void moveClass(int classIndex, int packageIndex)
	{
		int actualPackage = newPackage[classIndex];
		
		if (actualPackage != packageIndex)
		{
			removeClassInfluence(classIndex);
			newPackage[classIndex] = packageIndex;
			addClassInfluence(classIndex);
			//preparePackageDependencies();
			
			mq[actualPackage] = calculateModularizationFactor(actualPackage);
			mq[packageIndex] = calculateModularizationFactor(packageIndex);
		}
	}
	
	public void moveAll(int[] packageIndexes)
	{
		for (int i = 0; i < classCount; i++)
			newPackage[i] = packageIndexes[i];
		
		prepareMQ();
	}
	
	public double calculateModularizarionFactor()
	{
		double result = 0.0;
		
		for (int i = 0; i < packageCount; i++)
			result += mq[i];
		
		return result;
	}
}