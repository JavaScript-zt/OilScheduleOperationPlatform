/*
 * PolynomialOffspringGenerator.java
 *
 * @author Antonio J. Nebro
 * @version 1.0
 *
 * This class returns a solution after applying SBX and Polynomial mutation
 */
package org.uma.jmetal.util.offspring;

import org.uma.jmetal.core.Operator;
import org.uma.jmetal.core.Solution;
import org.uma.jmetal.operator.mutation.MutationFactory;
import org.uma.jmetal.operator.selection.SelectionFactory;
import org.uma.jmetal.util.JMException;

import java.util.HashMap;

public class PolynomialMutationOffspring extends Offspring {

	private Operator mutation_;
	private Operator selection_;
	
	private double mutationProbability_ ;
	private double distributionIndex_ ;

	public PolynomialMutationOffspring(double mutationProbability,
			double distributionIndexForMutation
			) throws JMException {
		HashMap  parameters ; // Operator parameters
		parameters = new HashMap() ;
		parameters.put("probability", mutationProbability_= mutationProbability) ;
		parameters.put("distributionIndex", distributionIndex_= distributionIndexForMutation) ;
		mutation_ = MutationFactory.getMutationOperator("PolynomialMutation", parameters);

		selection_ = SelectionFactory.getSelectionOperator("BinaryTournament", null);
		id_ = "PolynomialMutation";
	}

	public Solution getOffspring(Solution solution) {
		Solution res = new Solution(solution);
		try {
			mutation_.execute(res);
		} catch (JMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}
	
  public String configuration() {
  	String result = "-----\n" ;
  	result += "Operator: " + id_ + "\n" ;
  	result += "Probability: " + mutationProbability_ + "\n" ;
  	result += "DistributionIndex: " + distributionIndex_ ;
  	
  	return result ;
  }
} // PolynomialOffspringGenerator


