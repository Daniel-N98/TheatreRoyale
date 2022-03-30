package Tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import controller.Theatre;
import models.Patron;
import models.Performance;
import models.Seat;
import models.Seat.seatLoc;
import models.Ticket;
import util.DateTimeConverter;

class UserInteractionTesting {

	private String nowNow;
	private Performance testPerformance;
	private Seat testSeat;
	private ArrayList<Seat> testSeats;
	private Ticket testTicket;
	private DateTimeConverter dtConv;
	private Theatre testTheatre;
	private Patron albert;
	private ArrayList<Performance> performancesInSearch;

	@BeforeEach
	void setUp() throws Exception {
		// create theatre testing mode set to true
		testTheatre = new Theatre(true);
		// time converter
		dtConv = new DateTimeConverter();
		nowNow = LocalDateTime.now().toString();
		testTheatre.getShowByPerformanceID(99990999);
		performancesInSearch = testTheatre.getPerformancesInSearch();
//		System.out.println("number: " + performancesInSearch.size());
		testPerformance = performancesInSearch.get(0);
		albert = new Patron();
		testPerformance.getPrice();
		testSeats = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			testSeat = new Seat(testPerformance, seatLoc.Stall, 0);
			testSeats.add(testSeat);
		}
		testTicket = new Ticket(testPerformance, albert);
		testTicket.calcCost();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	public void seatTest() {
		String nowNow = LocalDateTime.now().toString();
		Seat testSeat = new Seat(testPerformance, seatLoc.Stall, 0);
		assertNotNull(testSeat);
	}

	@Test
	public void ticketPriceTest() {
		assertEquals(59.97, testTicket.getCost());
	}

	@Test
	public void testPerformanceStartDateTime() {
		String sdf = testPerformance.getStartDateTime();
		assertEquals("2025-01-01 23:59:59", sdf.toString());
	}

	@Test
	public void getPerformanceByDate() {
		performancesInSearch.clear();
		testTheatre.findShowsByDate_Test("01-01-25");
		performancesInSearch = testTheatre.getPerformancesInSearch();
		// System.out.println("number: " + performancesInSearch.size());
		testPerformance = performancesInSearch.get(0);
		assertEquals("Hamlet in the original Klingon", testPerformance.getTitle());
	}

	@Test
	public void addTicketsToBasketTest() {
		// get the patron
		Patron albert = testTheatre.getPatron();
		// get the performance
		albert.selectForBasket(testTicket);
		// get the the seats
		testTicket.addSeatsToTicket(5, 3);
		// get the price on the ticket
		testTicket.calcCost();
		// say no to postage
		testTicket.checkPostage(testPerformance);
		testTicket.acceptPostage();
		// process basket
		albert.acceptTicketToBasket(testTicket);
		// end the test
		double tmpTotal = albert.getBasket().getTotal();

		assertEquals(0, tmpTotal);

	}

	@Test
	public void subtractTicketsFromBasket() {
		// get the patron
		// get the performance
		// get the the seats
		// get the price on the ticket
		// say no to postage
		// // remove tickets
		// process basket
		// end the test
	}

	@Test
	public void postageTest() {
		testPerformance.setStartDateTime(nowNow);
		assertEquals(0, testTicket.getPostage());

	}
}
