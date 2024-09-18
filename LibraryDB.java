/*
 * An example Library DB model by Kieran Yee
 */
import java.sql.*;
import javax.swing.*;

public class LibraryDB {
    private JFrame dialogParent;
    private Connection con = null;

    public LibraryDB(JFrame parent, String userid, String password) {
		dialogParent = parent;
		try {
			Class.forName("org.postgresql.Driver");
			String url = "jdbc:postgresql:" + "//db.example.com/" + userid + "guest";
			System.out.println("Connection to server successful");
			try {
				con = DriverManager.getConnection(url, userid, password);
				con.setReadOnly(true);
				con.isReadOnly();
			} catch (SQLException sqler) {
				System.out.println("Connection error: " + sqler.getMessage());
			}
		} catch (ClassNotFoundException er) {
			System.out.println("JDBC Driver missing: " + er);
		}
	}

    //  READ execution
    public String bookLookup(int isbn) {
        
		String SQL = "SELECT a.surname, a.name " + "FROM author a, book_author b " + "WHERE a.authorid=  b.authorid "
				+ "AND ISBN = " + isbn + " ORDER BY b.authorseqno;";
		String book = "SELECT * FROM book WHERE ISBN = " + isbn;
		try {
			Statement s = con.createStatement();
			Statement s2 = con.createStatement();
			ResultSet rs = s.executeQuery(SQL);
			ResultSet rbook = s2.executeQuery(book);
			ResultSetMetaData rsmd = rs.getMetaData();
			try {
				rbook.next();
				String st = "Book Lookup for: " + rbook.getString("title") + "\n";
				st += "AUTHORS: \n";
				int cols = rsmd.getColumnCount();
				for (int i = 1; i <= cols; i++) {
					st += rsmd.getColumnName(i) + "   	";
				}
				st += "\n==========================\n";
				System.out.println(st);
				while (rs.next()) {
					String ln = rs.getString("Surname");
					String fn = rs.getString("Name");
					String out = ln + "," + fn;
					System.out.println(out);
					st += out + "\n";
				}
				return st;
			} catch (SQLException sqler) {
				System.out.println("An exception while processing the result (failure): " + sqler.getMessage());
			}
		} catch (SQLException sqer) {
			System.out.println("An exception probably SQL related: " + sqer.getMessage());
		}
		return "Book lookup Retrieval Failure";
	}

    public String showCatalogue() {
		String SQL = "SELECT isbn, title, edition_no, numleft AS copies_left FROM book WHERE isbn > 0 ORDER BY title;";
		try {
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery(SQL);
			ResultSetMetaData rsmd = rs.getMetaData();
			try {
				String st = "Book Catalogue:\n";
				int cols = rsmd.getColumnCount();
				for (int i = 1; i <= cols; i++) {
					if (rsmd.getColumnName(i).equals("title")) {
						st += rsmd.getColumnName(i) + " (edition no.)				";
						continue;
					}
					if (rsmd.getColumnName(i).equals("edition_no") || rsmd.getColumnName(i).equals("copies_left")) {
						continue;
					}
					st += rsmd.getColumnName(i) + "   ";
				}
				st += "\n===================================================\n";
				System.out.println(st);
				while (rs.next()) {
					int aid = rs.getInt("isbn");
					String ti = rs.getString("title");
					int ed = rs.getInt("edition_no");
					int noleft = rs.getInt("copies_left");
					String out = aid + "  " + ti.trim() + " (" + ed + ") - Copies remaining: " + noleft;
					System.out.println(out);
					st += out + "\n";
				}
				return st;
			} catch (SQLException sqler) {
				System.out.println("An exception while processing the result (failure): " + sqler.getMessage());
			}
		} catch (SQLException sqer) {
			System.out.println("An exception probably SQL related: " + sqer.getMessage());
		}
		return "All Books Retrieval Failure";
	}

    public String showLoanedBooks() {
		String SQL1 = "SELECT b.isbn, b.title, b.numleft FROM book b, cust_book c WHERE b.isbn = c.isbn;";
		String SQL2 = "SELECT cb.customerid, c.l_name AS surname, c.f_name AS firstname, cb.duedate FROM cust_book cb, customer c WHERE cb.customerid = c.customerid;";
		try {
			Statement s = con.createStatement();
			Statement s2 = con.createStatement();
			ResultSet rs = s.executeQuery(SQL1);
			ResultSet rs2 = s2.executeQuery(SQL2);
			try {
				String st = "Books currently under Loan:";
				st += "\n===================================================\n";
				System.out.println(st);
				while (rs.next()) {
					rs2.next();
					int isbn = rs.getInt("isbn");
					String ti = rs.getString("title");
					int noleft = rs.getInt("numleft");
					String book = "Book: (" + isbn + ") " + ti + "\nCopies left: " + noleft;
					int cid = rs2.getInt("customerid");
					String ln = rs2.getString("surname");
					String fn = rs2.getString("firstname");
					String dd = rs2.getString("duedate");
					String cust = "Borrower: (" + cid + ") " + ln + ", " + fn + "\n  Due back: " + dd + "\n";
					System.out.println(book + cust);
					st += book + "\n" + cust + "\n";
				}
				return st;
			} catch (SQLException sqler) {
				System.out.println("An exception while processing the result (failure): " + sqler.getMessage());
			}
		} catch (SQLException sqer) {
			System.out.println("An exception probably SQL related: " + sqer.getMessage());
		}
		return "Loaned Books Retrieval Failure";
	}

    public String showAuthor(int authorID) {
		String SQL1 = "SELECT authorid, surname, name FROM author WHERE AuthorID = " + authorID + ";";
		String SQL2 = "SELECT b.isbn, b.title, b.edition_no, b.numleft FROM book b JOIN book_author ba ON b.isbn = ba.isbn WHERE authorID = "
				+ authorID + ";";
		try {
			Statement s = con.createStatement();
			Statement s2 = con.createStatement();
			ResultSet rs = s.executeQuery(SQL1);
			ResultSet rs2 = s2.executeQuery(SQL2);
			try {
				String st = "Author Lookup:\n";
				System.out.println(st);
				while (rs.next()) {
					int aid = authorID;
					String fn = rs.getString("name");
					String ln = rs.getString("surname");
					String auth = "  (" + aid + ") - " + fn.trim() + " " + ln + "\n";
					System.out.println(auth);
					st += auth;
				}
				st += "Books by author:\n";
				while (rs2.next()) {
					int isbn = rs2.getInt("isbn");
					String ti = rs2.getString("title");
					int edn = rs2.getInt("edition_no");
					int noleft = rs2.getInt("numleft");
					String book = "    (" + isbn + ") - " + ti.trim() + ", Edition: " + edn + " (Remaining copies: "
							+ noleft + ")";
					System.out.println(book);
					st += book + "\n";
				}
				return st;
			} catch (SQLException sqler) {
				System.out.println("An exception while processing the result (failure): " + sqler.getMessage());
			}
		} catch (SQLException sqer) {
			System.out.println("An exception probably SQL related: " + sqer.getMessage());
		}
		return "Selected Author Retrieval Failure";
	}

    public String showAllAuthors() {
		String SQL = "SELECT authorid, surname, name FROM author WHERE AuthorID > 0;";
		try {
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery(SQL);
			ResultSetMetaData rsmd = rs.getMetaData();
			try {
				String st = "Authors List:\n";
				int cols = rsmd.getColumnCount();
				for (int i = 1; i <= cols; i++) {
					st += rsmd.getColumnName(i) + "   ";
				}
				st += "\n==========================\n";
				System.out.println(st);
				while (rs.next()) {
					int aid = rs.getInt("AuthorId");
					String ln = rs.getString("Surname");
					String fn = rs.getString("Name");
					String out = aid + "     	     " + ln.trim() + ", " + fn;
					System.out.println(out);
					st += out + "\n";
				}
				return st;
			} catch (SQLException sqler) {
				System.out.println("An exception while processing the result (failure): " + sqler.getMessage());
			}
		} catch (SQLException sqer) {
			System.out.println("An exception probably SQL related: " + sqer.getMessage());
		}
		return "All Authors Retrieval Failure";
	}

    public String showCustomer(int customerID) {
		String SQL1 = "SELECT customerid, l_name, f_name, city FROM customer WHERE customerid = " + customerID + ";";
		String SQL2 = "SELECT b.isbn, b.title, b.edition_no, c.duedate FROM book b JOIN cust_book c ON b.isbn = c.isbn WHERE c.customerid = "
				+ customerID + " ORDER BY b.title;";
		try {
			Statement s = con.createStatement();
			Statement s2 = con.createStatement();
			ResultSet rs = s.executeQuery(SQL1);
			ResultSet rs2 = s2.executeQuery(SQL2);
			try {
				String st = "Customer Lookup:\n";
				System.out.println(st);
				while (rs.next()) {
					int cid = customerID;
					String ln = rs.getString("l_name");
					String fn = rs.getString("f_name");
					String city = rs.getString("city");
					String cust = "  (" + cid + ") - " + ln.trim() + " " + fn.trim() + " - " + city + "\n";
					System.out.println(cust);
					st += cust;
				}
				st += "Books borrowed:\n";
				if (rs2.next() == false) {
					st += "    No books borrowed";
					return st;
				} else {

					do {
						int isbn = rs2.getInt("isbn");
						String ti = rs2.getString("title");
						int edn = rs2.getInt("edition_no");
						String dd = rs2.getString("duedate");
						String book = "    (" + isbn + ") - " + ti.trim() + ", Edition: " + edn + " - Due back: " + dd;
						System.out.println(book);
						st += book + "\n";
					} while (rs2.next());
				}
				return st;
			} catch (SQLException sqler) {
				System.out.println("An exception while processing the result (failure): " + sqler.getMessage());
			}
		} catch (SQLException sqer) {
			System.out.println("An exception probably SQL related: " + sqer.getMessage());
		}
		return "Customer Retrieval Failure";
	}

    public String showAllCustomers() {
		String SQL = "SELECT customerid AS CustID, l_name AS FullName, f_name AS FName, city as City FROM customer WHERE CustomerID > 0;";
		try {
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery(SQL);
			ResultSetMetaData rsmd = rs.getMetaData();
			try {
				String st = "Customers List:\n";
				int cols = rsmd.getColumnCount();
				for (int i = 1; i <= cols; i++) {
					if (rsmd.getColumnName(i).equals("fname")) {
						st += "    ";
						continue;
					}
					st += rsmd.getColumnName(i) + "    ";
				}
				st += "\n==========================\n";
				System.out.println(st);
				while (rs.next()) {
					int aid = rs.getInt("CustID");
					String ln = rs.getString("Fullname");
					String fn = rs.getString("FName");
					String city = rs.getString("City");
					String out = aid + "     	   " + fn.trim() + " " + ln.trim() + " - " + city;
					System.out.println(out);
					st += out + "\n";
				}
				return st;
			} catch (SQLException sqler) {
				System.out.println("An exception while processing the result (failure): " + sqler.getMessage());
			}
		} catch (SQLException sqer) {
			System.out.println("An exception probably SQL related: " + sqer.getMessage());
		}
		return "All Customers Retrieval Failure";
	}

    //  UPDATE execution
    public String borrowBook(int isbn, int customerID, int day, int month, int year) {        
		String CHECK = "SELECT COUNT(*) FROM customer WHERE customerID = " + customerID + ";";
		String CHECKBOOK = "SELECT COUNT(*) FROM book WHERE isbn = " + isbn + " AND numleft > 0;";
		String INS = "INSERT INTO cust_book VALUES (" + isbn + ",'" + year + "-" + month + "-" + day + "'," + customerID
				+ ");";
		String UPDATE = "UPDATE book SET numleft = numleft-1 WHERE isbn = " + isbn + ";";
		String SQ1 = "SELECT isbn, title FROM book WHERE ISBN = " + isbn + ";";
		String SQ2 = "SELECT customerid, f_name, l_name FROM customer WHERE customerid = " + customerID + ";";
		
        try {
			con.setReadOnly(false);
			con.setAutoCommit(false);
			Statement chk = con.createStatement();
			ResultSet rschk = chk.executeQuery(CHECK);

			while (rschk.next()) { // Checks whether the customer exists
				if (rschk.getInt(1) != 1) {
					String nocus = "Customer with custid: " + customerID + " does not exist.";
					System.out.println(nocus);
					return nocus;
				}
			}

			Statement lockcus = con.createStatement();
			String cuslock = "SELECT * FROM customer WHERE customerID = " + customerID + " FOR UPDATE;";
			lockcus.executeQuery(cuslock); // Locks customer
			Statement bkchk = con.createStatement();
			ResultSet rsbkchk = bkchk.executeQuery(CHECKBOOK);

			while (rsbkchk.next()) {
				if (rsbkchk.getInt(1) != 1) {
					String nobook = "Book with isbn: " + isbn + " is not available or does not exist.";
					System.out.println(nobook);
					con.rollback();
					con.setReadOnly(true);
					con.setAutoCommit(true);
					return nobook;
				}
			}

			Statement lockbk = con.createStatement();
			String bklock = "SELECT * FROM book WHERE isbn = " + isbn + " FOR UPDATE;";
			lockbk.executeQuery(bklock); // Locks book if it exists and is available
			PreparedStatement ps = con.prepareStatement(INS);
			int rows = ps.executeUpdate(); // Inserts the tuple into cust_book
			if (rows != 1) {
				con.rollback();
				con.setReadOnly(true);
				con.setAutoCommit(true);
				return "Book Borrow failed";
			}
			JOptionPane.showMessageDialog(dialogParent, "Continue?");
			PreparedStatement psbook = con.prepareStatement(UPDATE);
			int bkrow = psbook.executeUpdate(); // update book table 
			if (bkrow != 1) {
				con.rollback();
				con.setReadOnly(true);
				con.setAutoCommit(true);
				return "Failure updating book table";
			}

			try {
				Statement stmt = con.createStatement();
				Statement stmt2 = con.createStatement();
				ResultSet rs = stmt.executeQuery(SQ1);
				ResultSet rs2 = stmt2.executeQuery(SQ2);
				String st = "Book Borrowed by Customer:\n";
				System.out.println(st);
				while (rs.next()) {
					rs2.next();
					String ti = rs.getString("title");
					String fn = rs2.getString("f_name");
					String ln = rs2.getString("l_name");
					String out = "    (" + isbn + ") - " + ti.trim() + "\n    Loaned by: " + fn.trim() + " " + ln.trim()
							+ "(" + customerID + ")\n";
					out += "    Due back: " + day + "-" + month + "-" + year;
					System.out.println(out);
					st += out + "\n";
				}
				con.commit();
				con.setAutoCommit(true);
				con.setReadOnly(true);
				return st;
			} catch (SQLException sqler) {
				con.rollback();
				con.setReadOnly(true);
				con.setAutoCommit(true);
				System.out.println("An exception while processing the result (failure): " + sqler.getMessage());
			}
		} catch (SQLException sqer) {
			try {
				con.rollback();
				con.setReadOnly(true);
				con.setAutoCommit(true);
			} catch (SQLException e) {

				System.out.println("An exception probably SQL related: " + e.getMessage());
			}

			System.out.println("An exception probably SQL related: " + sqer.getMessage());
		}
		return "Book borrowing Failure";
	}

    public String returnBook(int isbn, int customerID) {
		String CHECK = "SELECT COUNT(*) FROM customer WHERE customerID = " + customerID + ";";
		String CHECKBOOK = "SELECT COUNT(*) FROM book WHERE isbn = " + isbn + ";";
		String UPDATE = "UPDATE book SET numleft = numleft+1 WHERE isbn = " + isbn + ";";
		String DEL = "DELETE FROM cust_book WHERE isbn = " + isbn + " AND customerid = " + customerID + ";";
		String SQ1 = "SELECT isbn, title FROM book WHERE ISBN = " + isbn + ";";
		String SQ2 = "SELECT customerid, f_name, l_name FROM customer WHERE customerid = " + customerID + ";";

		try {
			con.setReadOnly(false);
			con.setAutoCommit(false);
			Statement chk = con.createStatement();
			ResultSet rschk = chk.executeQuery(CHECK);

			while (rschk.next()) { // Check whether the customer exists
				if (rschk.getInt(1) != 1) {
					String nocus = "Customer with custid: " + customerID + " does not exist.";
					System.out.println(nocus);
					return nocus;
				}
			}

			Statement lockcus = con.createStatement();
			String cuslock = "SELECT * FROM customer WHERE customerID = " + customerID + " FOR UPDATE;";
			lockcus.executeQuery(cuslock); // Locks customer
			Statement bkchk = con.createStatement();
			ResultSet rsbkchk = bkchk.executeQuery(CHECKBOOK);

			while (rsbkchk.next()) {
				if (rsbkchk.getInt(1) != 1) {
					String nobook = "Book with isbn: " + isbn + " is not available or does not exist.";
					System.out.println(nobook);
					con.rollback();
					con.setReadOnly(true);
					con.setAutoCommit(true);
					return nobook;
				}
			}

			Statement lockbk = con.createStatement();
			String bklock = "SELECT * FROM book WHERE isbn = " + isbn + " FOR UPDATE;";
			lockbk.executeQuery(bklock); // Locks book since it exists and is available
			PreparedStatement ps = con.prepareStatement(DEL);
			int rows = ps.executeUpdate(); // Deletes tuple from cust_book
			if (rows != 1) {
				con.rollback();
				con.setReadOnly(true);
				con.setAutoCommit(true);
				return "Book return failed, no record of loaned book";
			}
			JOptionPane.showMessageDialog(dialogParent, "Continue?");
			PreparedStatement psbook = con.prepareStatement(UPDATE);
			int bkrow = psbook.executeUpdate(); // update book table
			if (bkrow != 1) {
				con.rollback();
				con.setReadOnly(true);
				con.setAutoCommit(true);
				return "Failure updating book table";
			}

			try {
				Statement stmt = con.createStatement();
				Statement stmt2 = con.createStatement();
				ResultSet rs = stmt.executeQuery(SQ1);
				ResultSet rs2 = stmt2.executeQuery(SQ2);
				String out = "";
				while (rs.next()) {
					rs2.next();
					String ti = rs.getString("title");
					String fn = rs2.getString("f_name");
					String ln = rs2.getString("l_name");
					out = "Book (" + isbn + ") - '" + ti.trim() + "' returned by: " + fn.trim() + " " + ln.trim() + "("
							+ customerID + ")\n";
					System.out.println(out);
				}
				con.commit();
				con.setAutoCommit(true);
				con.setReadOnly(true);
				return out;
			} catch (SQLException sqler) {
				con.rollback();
				con.setReadOnly(true);
				con.setAutoCommit(true);
				System.out.println("An exception while processing the result (BAD): " + sqler.getMessage());
			}
		} catch (SQLException sqer) {
			try {
				con.rollback();
				con.setReadOnly(true);
				con.setAutoCommit(true);
			} catch (SQLException e) {
				System.out.println("An exception probably SQL related: " + e.getMessage());
			}

			System.out.println("An exception probably SQL related: " + sqer.getMessage());
		}
		return "Failed to return book";
	}

    //  DELETE execution
    public String deleteCus(int customerID) {
		String DEL = "DELETE FROM customer WHERE customerid = " + customerID + ";";
		String SQ1 = "SELECT customerid, f_name, l_name FROM customer WHERE customerid = " + customerID + ";";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(SQ1);
			rs.next();
			String fn = rs.getString("f_name");
			String ln = rs.getString("l_name");
			PreparedStatement ps = con.prepareStatement(DEL);
			int rows = ps.executeUpdate();
			if (rows != 1) {
				return "Unable to delete customer: (" + customerID + ") " + fn.trim() + " " + ln.trim()
						+ ", books still loaned";
			}

			String out = "Customer: (" + customerID + ") " + fn.trim() + " " + ln.trim() + " removed from database.";
			return out;
		} catch (SQLException sqler) {
			System.out.println("An exception probably SQL related: " + sqler.getMessage());
		}
		return "Customer deletion failed, either customer does not exist or books still due";
	}

    public String deleteAuthor(int authorID) {
		String DEL = "DELETE FROM author WHERE authorid = " + authorID + ";";
		String SQ1 = "SELECT a.authorid, a.surname, a.name FROM author a JOIN book_author ba ON a.authorid = ba.authorid WHERE a.authorid = "
				+ authorID + ";";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(SQ1);
			rs.next();
			String fn = rs.getString("surname");
			String ln = rs.getString("name");
			PreparedStatement ps = con.prepareStatement(DEL);
			int rows = ps.executeUpdate();
			if (rows != 1) {
				return "Unable to delete author: (" + authorID + ") " + ln.trim() + ", " + fn.trim()
						+ " authorid error";
			}
			String out = "Author: (" + authorID + ") " + ln.trim() + ", " + fn.trim() + " removed from database.";
			return out;
		} catch (SQLException sqler) {
			System.out.println("An exception probably SQL related: " + sqler.getMessage());
		}
		return "Author deletion failed";
	}

	public String deleteBook(int isbn) {
		String DEL = "DELETE FROM book WHERE isbn = " + isbn + ";";
		String SQ1 = "SELECT b.title, b.edition_no FROM book b WHERE isbn = " + isbn + ";";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(SQ1);
			rs.next();
			String ti = rs.getString("title");
			String edn = rs.getString("edition_no");
			PreparedStatement ps = con.prepareStatement(DEL);
			int rows = ps.executeUpdate();
			if (rows != 1) {
				return "Unable to delete book: (" + isbn + ") " + ti.trim() + ", Ed: " + edn + " error";
			}
			String out = "Book: (" + isbn + ") " + ti.trim() + ", Edition: " + edn.trim() + " removed from database.";
			return out;
		} catch (SQLException sqler) {
			System.out.println("An exception probably SQL related: " + sqler.getMessage());
		}
		return "Book deletion failed";
	}

    public void closeDBConnection() {
		if (con != null) {
			try {
				con.close();
				System.out.println("Connection to server closed.");
			} catch (SQLException e) {
				System.out.println("connection closure error: " + e);
			}
		}
	}
}