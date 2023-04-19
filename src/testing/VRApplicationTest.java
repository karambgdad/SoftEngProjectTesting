package testing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;

import application.VRApplication;
import dbadapter.DBFacade;
import junit.framework.TestCase;

public class VRApplicationTest extends TestCase {
	
	public VRApplicationTest() {
		super();
	}
	
	@Test
	public void testCheckPayment() {
		DBFacade stub = mock(DBFacade.class);
		DBFacade.setInstance(stub);
		
		VRApplication.getInstance().checkPayment();
		
		verify(stub, times(1)).setAvailableHolidayOffer();
	}

}
