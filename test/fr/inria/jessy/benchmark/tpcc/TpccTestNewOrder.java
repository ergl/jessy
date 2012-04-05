package fr.inria.jessy.benchmark.tpcc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import fr.inria.jessy.LocalJessy;
import fr.inria.jessy.benchmark.tpcc.entities.*;
import fr.inria.jessy.transaction.*;

/**
 * @author WANG Haiyun & ZHAO Guang
 * 
 */
public class TpccTestNewOrder {

	LocalJessy jessy;
	InsertData id;
	NewOrder no; 
	Warehouse wh;
	District di;
	Item it;
	Customer cu;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		jessy = LocalJessy.getInstance();

		jessy.addEntity(Warehouse.class);
		jessy.addEntity(District.class);
		jessy.addEntity(Customer.class);
		jessy.addEntity(Item.class);
		jessy.addEntity(Stock.class);
		jessy.addEntity(History.class);
		jessy.addEntity(Order.class);
		jessy.addEntity(New_order.class);
		jessy.addEntity(Order_line.class);
		id = new InsertData(jessy);
		id.execute();
		no = new NewOrder(jessy);
	}

	/**
	 * Test method for {@link fr.inria.jessy.BenchmarkTpcc.NewOrder}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testNewOrder() throws Exception {

		ExecutionHistory result = no.execute();
		/* test execution */
		assertEquals("Result", TransactionState.COMMITTED,
				result.getTransactionState());

			

	}

}
