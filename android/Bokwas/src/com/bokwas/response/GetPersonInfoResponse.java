package com.bokwas.response;

public class GetPersonInfoResponse {
	
	public PersonInfo person_details;
	public ApiResponse status;
	
	public class PersonInfo {
		public String bokwas_name;
		public String bokwas_avatar_id;
	}

}
