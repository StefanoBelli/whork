package logic.bean;

import java.util.Date;
import java.util.regex.Pattern;
import java.io.Serializable;
import java.io.File;
import logic.exception.SyntaxException;

public class UserBean implements Serializable {
	private static final long serialVersionUID = -3927240997879942530L;

	private String cf;
	private String name;
	private String surname;
	private String phoneNumber;
	private boolean employee;
	private CompanyBean company;
	private boolean admin;
	private boolean recruiter;
	private String note;
	private File photo;
	private Date birthday;
	private File cv;
	private String homeAddress;
	private String biography;
	private ComuneBean comune;
	private EmploymentStatusBean employmentStatus;

	public String getCf() {
		return cf;
	}

	public String getName() {
		return name;
	}

	public String getSurname() {
		return surname;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public boolean isEmployee() {
		return employee;
	}

	public CompanyBean getCompany() {
		return company;
	}

	public boolean isAdmin() {
		return admin;
	}

	public boolean isRecruiter() {
		return recruiter;
	}

	public String getNote() {
		return note;
	}

	public File getPhoto() {
		return photo;
	}

	public Date getBirthday() {
		return birthday;
	}

	public File getCv() {
		return cv;
	}

	public String getHomeAddress() {
		return homeAddress;
	}

	public String getBiography() {
		return biography;
	}

	public ComuneBean getComune() {
		return comune;
	}

	public EmploymentStatusBean getEmploymentStatus() {
		return employmentStatus;
	}

	public void setCf(String cf) 
			throws SyntaxException {
		if(cf.length() != 16) {
			throw new SyntaxException("CF length must be equal to 16");
		}
		this.cf = cf;
	}

	public void setName(String name) 
			throws SyntaxException {
		if(name.length() > 45) {
			throw new SyntaxException("Name length must be max. 45 chars");
		}

		this.name = name;
	}

	public void setSurname(String surname) 
			throws SyntaxException {
		if(surname.length() > 45) {
			throw new SyntaxException("Surname length must be max. 45 chars");
		}

		this.surname = surname;
	}

	public void setPhoneNumber(String phoneNumber) 
			throws SyntaxException {
		if(!Pattern.matches("^\\+(?:[0-9] ?){6,14}[0-9]$", phoneNumber)) {
			throw new SyntaxException("Phone number is not valid");
		}
		
		this.phoneNumber = phoneNumber;
	}

	public void setEmployee(boolean employee) {
		this.employee = employee;
	}

	public void setCompany(CompanyBean company) {
		this.company = company;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public void setRecruiter(boolean recruiter) {
		this.recruiter = recruiter;
	}

	public void setNote(String note) 
			throws SyntaxException {
		if(note.length() > 45) {
			throw new SyntaxException("Note length cannot be greater than 45");
		}

		this.note = note;
	}

	public void setPhoto(File photo) {
		this.photo = photo;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public void setCv(File cv) {
		this.cv = cv;
	}

	public void setHomeAddress(String homeAddress) 
			throws SyntaxException {
		if(homeAddress.length() > 45) {
			throw new SyntaxException("Home address length cannot be greater than 45");
		}

		this.homeAddress = homeAddress;
	}

	public void setBiography(String biography) 
			throws SyntaxException {
		if(homeAddress.length() > 250) {
			throw new SyntaxException("Biography length cannot be greater than 250");
		}
		this.biography = biography;
	}
	
	public void setComune(ComuneBean comune) {
		this.comune = comune;
	}

	public void setEmploymentStatus(EmploymentStatusBean employmentStatus) {
		this.employmentStatus = employmentStatus;
	}
}