package medsavant.discovery;

import com.healthmarketscience.sqlbuilder.BinaryCondition;
import com.healthmarketscience.sqlbuilder.Condition;
import com.healthmarketscience.sqlbuilder.UnaryCondition;
import java.util.Map;
import javax.swing.JTextField;

/**
 * From the filter panes, stores details that are required for building 
 * ComboConditions at runtime.
 * 
 * @author rammar
 */
public class FilterDetails {
	private String variantProperty;
	private String operator;
	private JTextField jtf;
	private DiscoveryPanel dp;		
	

	public FilterDetails(DiscoveryPanel dp) {
		this.dp= dp;
	}

	
	/**
	 * Set the FilterDetails fields.
	 * @param variantProperty The name of the variant property used for filtering
	 * @param operator The logical operator applied
	 * @param jtf the JTextField where the condition is being specified
	 */
	public void setDetails(String variantProperty, String operator, JTextField jtf) {
		this.variantProperty= variantProperty;
		this.operator= operator;
		this.jtf= jtf;			
	}

	
	/**
	 * Get the current Condition object based on this filter.
	 */
	public Condition getCurrentCondition() {
		Condition c= null;
		
		DiscoveryFindings discFind= dp.getDiscoveryFindings();
		Map<String, String> columns= discFind.dbAliasToColumn;

		if (columns.get(variantProperty) != null) {
			if (operator.equals(DiscoveryPanel.LIKE_KEYWORD)) {
				/* Special case: For the LIKE operator, a "%" wildcard is 
				 * appended to the beginning and end of the text from the 
				 * text field. */
				c= BinaryCondition.iLike(discFind.ts.getDBColumn(columns.get(variantProperty)), "%" + jtf.getText() + "%");
			} else if (operator.equals(DiscoveryPanel.EQUALS_KEYWORD)) {
				c= BinaryCondition.equalTo(discFind.ts.getDBColumn(columns.get(variantProperty)), jtf.getText());
			} else if (operator.equals(DiscoveryPanel.LESS_KEYWORD)) {
				c= BinaryCondition.lessThan(discFind.ts.getDBColumn(columns.get(variantProperty)), jtf.getText(), false);
			} else if (operator.equals(DiscoveryPanel.GREATER_KEYWORD)) {
				c= BinaryCondition.greaterThan(discFind.ts.getDBColumn(columns.get(variantProperty)), jtf.getText(), false);
			} else if (operator.equals(DiscoveryPanel.EXIST_KEYWORD)) {
				c= UnaryCondition.isNotNull(discFind.ts.getDBColumn(columns.get(variantProperty)));
			}	
		}

		return c;
	}
}
