package fr.inria.jessy.benchmark.tpcc;

import java.util.Date;
import java.util.Random;

import fr.inria.jessy.Jessy;
import fr.inria.jessy.benchmark.tpcc.entities.Customer;
import fr.inria.jessy.benchmark.tpcc.entities.District;
import fr.inria.jessy.benchmark.tpcc.entities.Item;
import fr.inria.jessy.benchmark.tpcc.entities.New_order;
import fr.inria.jessy.benchmark.tpcc.entities.Order;
import fr.inria.jessy.benchmark.tpcc.entities.Order_line;
import fr.inria.jessy.benchmark.tpcc.entities.Stock;
import fr.inria.jessy.benchmark.tpcc.entities.Warehouse;
import fr.inria.jessy.transaction.ExecutionHistory;
import fr.inria.jessy.transaction.Transaction;

/**
 * @author Wang Haiyun & ZHAO Guang
 * 
 */
public class NewOrder extends Transaction {
	
	private String warhouseNumber;

	/* this class is written according to tpcc section 2.4.2 */
	public NewOrder(Jessy jessy, int warhouseNumber) throws Exception {
		super(jessy);
		this.warhouseNumber = "" + warhouseNumber;
	}

	@Override
	public ExecutionHistory execute() {
		try {
			Random rand = new Random(System.currentTimeMillis());
			Warehouse wh;
			District dis;
			Customer cus;
			New_order no;
			Order o;
			Order_line ol;
			Item it;
			Stock st;
			String district_id;
			String customer_id;
			NURand nu;

			int O_OL_CNT = rand.nextInt(15 - 5 + 1) + 5;
			int OL_QUANTITY;
			int warehouse_count, remote_warehouse_number, i, j, k;
			// we have 1% chance(when x == 1) that the ol_supply_warehouse is a remote warehouse
			int x;
			x = rand.nextInt(100);

			/* generate how many items we have in this new order, [5..15] */
			int ol_cnt = rand.nextInt(15 - 5 + 1) + 5;

			// wh = read(Warehouse.class, "W_"+ this.warhouseNumber);
			/*
			 * The district number (D_ID) is randomly selected within [1 .. 10]
			 * from the home warehouse (D_W_ID = W_ID).
			 */
			district_id = Integer.toString(rand.nextInt(10) + 1);
			dis = read(District.class, "D_W_" + warhouseNumber + "_D_"
					+ district_id);
			/*
			 * The non-uniform random customer number (C_ID) is selected using
			 * the NURand (1023,1,3000) function from the selected district
			 * number (C_D_ID = D_ID) and the home warehouse number (C_W_ID =
			 * W_ID).
			 */
			nu = new NURand(1023, 1, 3000);
			customer_id = Integer.toString(nu.calculate());
			cus = read(Customer.class,
					"C_W_" + warhouseNumber + "_C_D_" + dis.getD_ID() + "_C_"
							+ customer_id);

			/* setting up an org.imdea.benchmark.rubis.entity in New_order */
			no = new New_order("NO_W_" + warhouseNumber + "_NO_D_"
					+ dis.getD_ID() + "_NO_O_" + dis.getD_NEXT_O());
			no.setNO_W_ID(warhouseNumber);
			no.setNO_D_ID(dis.getD_ID());
			no.setNO_O_ID(dis.getD_NEXT_O());
			write(no);

			/* setting up an org.imdea.benchmark.rubis.entity in Order */
			o = new Order("O_W_" + warhouseNumber + "_O_D_" + dis.getD_ID()
					+ "_O_" + dis.getD_NEXT_O());
			o.setO_C_ID(cus.getC_ID());
			o.setO_D_ID(dis.getD_ID());
			o.setO_W_ID(warhouseNumber);
			o.setO_ID(dis.getD_NEXT_O());
			o.setO_CARRIER_ID(null);
			/*
			 * TODO How can we get current sys time?
			 */
			o.setO_ENTRY_D(new Date());
			o.setO_ALL_LOCAL(1);
			o.setO_OL_CNT(O_OL_CNT);

			write(o);

			dis.setD_NEXT_O(dis.getD_NEXT_O() + 1);
			write(dis);

			/* for each item in this new order, insert an org.imdea.benchmark.rubis.entity in Order_line */
			for (i = 0; i < ol_cnt; i++) {
				int OL_I_ID;
				
				/*
				 * A fixed 1% of the NewOrder transactions are chosen at random
				 * to simulate user data entry errors and exercise the
				 * performance of rolling back update transactions.
				 */
				int rbk = rand.nextInt(100);
				nu = new NURand(8191, 1, 100000);
				OL_I_ID = nu.calculate();
				it = read(Item.class, "I_" + OL_I_ID);
				st = read(Stock.class, "S_W_" + warhouseNumber + "_S_I_"
						+ OL_I_ID);
				/* generate quantity of this item, [1..10] */
				OL_QUANTITY = rand.nextInt(10) + 1;

				/* Decrease the stock of this item */
				if (st.getS_QUANTITY() >= OL_QUANTITY + 10) {
					st.setS_QUANTITY(st.getS_QUANTITY() - OL_QUANTITY);
				} else {
					st.setS_QUANTITY(st.getS_QUANTITY() - OL_QUANTITY + 91);
				}

				st.setS_YTD(st.getS_YTD() + OL_QUANTITY);
				st.setS_ORDER_CNT(st.getS_ORDER_CNT() + 1);
				/*
				 * OL_NUMBER(last attribute in the constructor) is set to a
				 * unique value within all the ORDER-LINE rows that have the
				 * same OL_O_ID value here my solution is put "i" in this field,
				 * because i is from 0-14, and it's unique
				 */
				ol = new Order_line("OL_W_" + warhouseNumber + "_OL_D_"
						+ dis.getD_ID() + "_OL_O_" + o.getO_ID() + "_OL_" + i);
				ol.setOL_AMOUNT(OL_QUANTITY * it.getI_PRICE());
				// ignored
				// Pattern p = Pattern.compile("*ORIGINAL*");
				// Matcher m1 = p.matcher(it.getI_DATA());
				// Matcher m2 = p.matcher(st.getS_DATA());
				// if(m1.find() && m2.find()) {
				// /*TODO
				// * the brand-generic field for that item is set to "B"
				// * I didn't find this field in any class
				// * */
				//
				// }
				// else{
				// /*otherwise, the brand-generic field is set to "G"*/
				// }
				//

				String[] dis_info = { st.getS_DIST_01(), st.getS_DIST_02(),
						st.getS_DIST_03(), st.getS_DIST_04(),
						st.getS_DIST_05(), st.getS_DIST_06(),
						st.getS_DIST_07(), st.getS_DIST_08(),
						st.getS_DIST_09(), st.getS_DIST_10() };
				ol.setOL_DIST_INFO(dis_info[Integer.parseInt(dis.getD_ID()) - 1]);
				ol.setOL_DELIVERY_D(null);
				ol.setOL_I_ID(it.getI_ID());
				ol.setOL_QUANTITY(OL_QUANTITY);
				ol.setOL_W_ID(warhouseNumber);
				ol.setOL_D_ID(dis.getD_ID());
				if(x == 1){//1% chance is a remote supply warehouse
					warehouse_count = 1;
					j = 0;
					while(j == 0){//calculate how many warehouses do we have in the DB
						wh = read(Warehouse.class, "W_"+warehouse_count);
						if(wh != null)
							warehouse_count++;
						else
							j = 1;
					}
					k = 0;
					//randomly choose 1 remote warehouses from the DB by checking we have more than one warehouse
					while(warehouse_count > 1 && k == 0){
						remote_warehouse_number = rand.nextInt(warehouse_count)+1;
						//make sure this is not the home warehouse
						if(remote_warehouse_number != Integer.parseInt(warhouseNumber)){ 
							k = 1;
							ol.setOL_SUPPLY_W_ID(Integer.toString(remote_warehouse_number));
						}
					}
					ol.setOL_SUPPLY_W_ID(warhouseNumber);//if we have only 1 warehouse, we have no remote warehouse
										
				}
				else{//99% chance home supply warehouse
					ol.setOL_SUPPLY_W_ID(warhouseNumber);
				}
				write(ol);

				/*
				 * if this item meet the roll back condition, we perform the
				 * roll back
				 */
				if (i == (ol_cnt - 1) && rbk == 1) {
					return abortTransaction();

				}
			}
			return commitTransaction();
		} catch (Exception ex) {
			ex.printStackTrace();
			return abortTransaction();
		}
	}

}
