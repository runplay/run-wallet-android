package run.wallet.common;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashMap;


public class PersonFull {
	

	private String id;
	private String name;
	private String lookupKey;

	private String nickname;
	private HashMap<String,String> numbers;
	private HashMap<String,String> emails;
	private ArrayList<String> notes;
	private String group;
	private String relationship;
	private String company;
	private String jobtitle;
	private String IM;
	private HashMap<String,HashMap<addressField,String>> address;
	private long thumbnailId;
	//private Bitmap thumbnail;
	
	//private static Bitmap blankIcon;
	
	public static final String TYPE_CNUM_MAIN="Main";
	public static final String TYPE_CEMAIL="Main";


    private boolean hasOtherData;
	
	public PersonFull() {
		
	}
	public String getLookupKey() {
		return lookupKey;
	}


	public void setLookupKey(String lookupKey) {
		this.lookupKey = lookupKey;
	}

    public void setThumbnailId(long thumbnailId) {
        this.thumbnailId = thumbnailId;
    }

    public enum addressField {
		street,city,state,postcode
		,country,type,pobox;
	}

    public long getThumbnailId() {
        return thumbnailId;
    }
    /*
    public Bitmap getThumbnailLarge(Context context) {
        if(thumbnail==null) {
            if(blankIcon==null)
                blankIcon=Bitmap.createScaledBitmap(BitmapFactory.decodeResource(context.getResources(),R.drawable.social_person), 160, 160, false);
            return blankIcon;
        }
        return thumbnail;
    }
    */
	//public void setThumbnail(Bitmap thumbnail) {
	//	this.thumbnail = thumbnail;
	//}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNickname() {
		return nickname!=null?nickname:(name!=null?name:"");
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public void addNumber(String type, String number) {
		if(numbers==null)
			numbers=new HashMap<String,String>();
		numbers.put(type, number);
	}
	public HashMap<String, String> getNumbers() {
		return numbers;
	}
	public void setNumbers(HashMap<String, String> numbers) {
		this.numbers = numbers;
	}
	public void addEmail(String type, String email) {
		if(emails==null)
			emails=new HashMap<String,String>();
		emails.put(type, email);
	}
	public HashMap<String, String> getEmails() {
		return emails;
	}
	public void setEmails(HashMap<String, String> emails) {
		this.emails = emails;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public String getRelationship() {
		return relationship;
	}
	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}
	public ArrayList<String> getNotes() {
		return notes;
	}
	public void setNotes(ArrayList<String> notes) {
		this.notes = notes;
	}
	public String getCompany() {
		return company;
	}
	public void setCompany(String company) {
		this.company = company;
	}
	public String getJobtitle() {
		return jobtitle;
	}
	public void setJobtitle(String jobtitle) {
		this.jobtitle = jobtitle;
	}
	public String getIM() {
		return IM;
	}
	public void setIM(String iM) {
		IM = iM;
	}
	public void addAddress(String type, HashMap<addressField,String> addressItem) {
		if(address==null)
			address=new HashMap<String, HashMap<addressField,String>>();
		address.put(type, addressItem);
	}
	public HashMap<String, HashMap<addressField,String>> getAddress() {
		return address;
	}
	public void setAddress(HashMap<String, HashMap<addressField,String>> address) {
		this.address = address;
	}

	public String getMainNumber() {
		if(getNumbers()!=null) {
			String mainnumber=getNumbers().get(PersonFull.TYPE_CNUM_MAIN);
			if(mainnumber!=null)
				return mainnumber;
		}
		return "";
	}
	public String getMainEmail() {
		if(getEmails()!=null) {
			String mainemail=getEmails().get(PersonFull.TYPE_CEMAIL);
			if(mainemail!=null)
				return mainemail;
		}
		return "";
	}


    public boolean hasOtherData() {
        return hasOtherData;
    }
    public void loadOtherData(Activity activity) {


        ContentResolver cr = activity.getContentResolver();

        String noteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] noteWhereParams = new String[]{getId(),
                ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
        Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);
        if(noteCur.getCount()>0) {
            ArrayList<String> notes=new ArrayList();
            while (noteCur.moveToNext()) {
                String note=noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                if(note!=null && note.length()>0)
                    notes.add(note);
                //Log.e("LOADOTHER","note: "+noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)));
            }
            setNotes(notes);
        }
        noteCur.close();
        //Log.e("CC", "helllllllllllllllloooooooooooooooooooooooo");
        //Get Postal Address....

        String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] addrWhereParams = new String[]{getId(),
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
        Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI,null, addrWhere, addrWhereParams, null);
        while(addrCur.moveToNext()) {
            HashMap<addressField,String> address=new HashMap<addressField,String>();
            address.put(PersonFull.addressField.pobox, addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX)));
            address.put(PersonFull.addressField.street, addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)));
            address.put(PersonFull.addressField.city, addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
            address.put(PersonFull.addressField.state,addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)));
            address.put(PersonFull.addressField.postcode, addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
            address.put(PersonFull.addressField.country, addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)));

            addAddress(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)), address);
        }
        addrCur.close();

        //Log.e("CC","helllllllllllllllloooooooooooooooooooooooo222222222222222222222");
        // Get Instant Messenger.........
        // Get Organizations.........

        String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
        String[] orgWhereParams = new String[]{getId(),
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
        Cursor orgCur = cr.query(ContactsContract.Data.CONTENT_URI, null, orgWhere, orgWhereParams, null);
        if (orgCur.moveToFirst()) {
            setCompany(orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA)));
            setJobtitle(orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE)));
        }
        orgCur.close();

        hasOtherData=true;
    }
}
