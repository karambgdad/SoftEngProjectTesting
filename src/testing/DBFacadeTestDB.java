package testing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import datatypes.AddressData;
import datatypes.GuestData;
import dbadapter.Booking;
import dbadapter.Configuration;
import dbadapter.DBFacade;
import dbadapter.HolidayOffer;
import junit.framework.TestCase;

/**
 * Testing our DBFacade.
 * 
 * @author karam
 *
 */
public class DBFacadeTestDB extends TestCase {

	private HolidayOffer testHO;
	private Booking testB;

	/**
	 * Preparing classes with static methods
	 */
	@Before
	public void setUp() {

		// HolidayOffer object to be tested
		testHO = new HolidayOffer(1, Timestamp.valueOf("2021-01-01 00:00:00"), Timestamp.valueOf("2021-12-31 00:00:00"),
				new AddressData("Oststr.99", "Duisburg"), 3, 50);
		testB = new Booking(1, Timestamp.valueOf("2021-01-01 00:00:00"), Timestamp.valueOf("2021-02-01 00:00:00"),
				Timestamp.valueOf("2021-02-28 00:00:00"), true, new GuestData("Peter", "peter@peter.de"), 1350, 1);
		ArrayList<Booking> testBookings = new ArrayList<Booking>();
		testBookings.add(testB);
		testHO.setBookings(testBookings);

		// SQL statements
		String sqlCleanDB = "DROP TABLE IF EXISTS booking,holidayoffer";
		String sqlCreateTableBooking = "CREATE TABLE booking (id int(11) NOT NULL AUTO_INCREMENT, creationDate timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, arrivalTime timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', departureTime timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', paid tinyint(1) NOT NULL, name varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL, email varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL, price double NOT NULL,hid int(11) NOT NULL, PRIMARY KEY(id));";
		String sqlCreateTableHolidayOffer = "CREATE TABLE holidayoffer (id int(11) NOT NULL AUTO_INCREMENT, startTime timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', endTime timestamp NOT NULL DEFAULT '0000-00-00 00:00:00', street varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL, town varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL, capacity int(11) NOT NULL, fee double NOT NULL, PRIMARY KEY (id));";
		String sqlInsertOffer = "INSERT INTO holidayoffer (id,startTime,endTime,street,town,capacity,fee) VALUES (?,?,?,?,?,?,?)";
		String sqlInsertBooking = "INSERT INTO booking (id,creationdate,arrivalTime,departureTime,paid,name,email,price,hid) VALUES (?,?,?,?,?,?,?,?,?)";

		// Perform database updates
		try (Connection connection = DriverManager
				.getConnection(
						"jdbc:" + Configuration.getType() + "://" + Configuration.getServer() + ":"
								+ Configuration.getPort() + "/" + Configuration.getDatabase(),
						Configuration.getUser(), Configuration.getPassword())) {

			try (PreparedStatement psClean = connection.prepareStatement(sqlCleanDB)) {
				psClean.executeUpdate();
			}
			try (PreparedStatement psCreateBooking = connection.prepareStatement(sqlCreateTableBooking)) {
				psCreateBooking.executeUpdate();
			}
			try (PreparedStatement psCreateHolidayOffer = connection.prepareStatement(sqlCreateTableHolidayOffer)) {
				psCreateHolidayOffer.executeUpdate();
			}
			try (PreparedStatement psInsertOffer = connection.prepareStatement(sqlInsertOffer)) {
				psInsertOffer.setInt(1, testHO.getId());
				psInsertOffer.setTimestamp(2, testHO.getStartTime());
				psInsertOffer.setTimestamp(3, testHO.getEndTime());
				psInsertOffer.setString(4, testHO.getAddressData().getStreet());
				psInsertOffer.setString(5, testHO.getAddressData().getTown());
				psInsertOffer.setInt(6, testHO.getCapacity());
				psInsertOffer.setDouble(7, testHO.getFee());
				psInsertOffer.executeUpdate();
			}
			try (PreparedStatement psInsertBooking = connection.prepareStatement(sqlInsertBooking)) {
				psInsertBooking.setInt(1, testB.getId());
				psInsertBooking.setTimestamp(2, testB.getCreationDate());
				psInsertBooking.setTimestamp(3, testB.getArrivalTime());
				psInsertBooking.setTimestamp(4, testB.getDepartureTime());
				psInsertBooking.setBoolean(5, testB.isPaid());
				psInsertBooking.setString(6, testB.getGuestData().getName());
				psInsertBooking.setString(7, testB.getGuestData().getEmail());
				psInsertBooking.setDouble(8, testB.getPrice());
				psInsertBooking.setInt(9, testB.getHid());
				psInsertBooking.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Testing getAvailableHolidayOffers with non-empty results.
	 */
	@Test
	public void testGetAvailableHolidayOffers() {

		// Select a time where the offer should be available
		Timestamp arrivalTime = Timestamp.valueOf("2021-03-01 00:00:00");
		Timestamp departureTime = Timestamp.valueOf("2021-03-15 00:00:00");
		int persons = 2;

		ArrayList<HolidayOffer> hos = DBFacade.getInstance().getAvailableHolidayOffers(arrivalTime, departureTime,
				persons);

		// Verify return values
		assertTrue(hos.size() == 1);
		assertTrue(hos.get(0).getId() == testHO.getId());
		assertTrue(hos.get(0).getBookings().size() == 1);
		assertTrue(hos.get(0).getFee() == testHO.getFee());
		// ...

	}

	/**
	 * Testing getAvailableHolidayOffer with empty result.
	 */
	@Test
	public void testGetAvailableHolidayOffersEmpty() {

		// Select a time where already a booking exists
		Timestamp arrivalTime = Timestamp.valueOf("2021-02-15 00:00:00");
		Timestamp departureTime = Timestamp.valueOf("2021-02-20 00:00:00");
		int persons = 2;

		ArrayList<HolidayOffer> hos = DBFacade.getInstance().getAvailableHolidayOffers(arrivalTime, departureTime,
				persons);

		// Verify return values

		assertTrue(hos.size() == 0);

	}

	/**
	 * Rest database
	 */

	@After
	public void tearDown() {

		// SQL statements
		String sqlCleanDB = "DROP TABLE IF EXISTS booking,holidayoffer";

		// Perform database updates
		try (Connection connection = DriverManager
				.getConnection(
						"jdbc:" + Configuration.getType() + "://" + Configuration.getServer() + ":"
								+ Configuration.getPort() + "/" + Configuration.getDatabase(),
						Configuration.getUser(), Configuration.getPassword())) {

			try (PreparedStatement psClean = connection.prepareStatement(sqlCleanDB)) {
				psClean.executeUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
