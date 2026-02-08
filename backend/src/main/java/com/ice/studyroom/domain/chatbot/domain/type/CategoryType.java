package com.ice.studyroom.domain.chatbot.domain.type;

public enum CategoryType {
	// 1. enum 상수 선언 (route 주소, notion url)
	RESERVATION("/reservation/room","https://www.notion.so/RESERVATION-2ff817bcfcdc80e1a7b7db15fba07c3b"),
	CHECKIN_QR("/attendance","https://www.notion.so/CHECKIN_QR-QR-2ff817bcfcdc8024b6d6e1db76d21af9"),
	EXTEND("/reservation/manage","https://www.notion.so/EXTEND-2ff817bcfcdc802084a9ce3c7ab99de5"),
	CANCEL_CHANGE("/reservation/manage","https://www.notion.so/CANCEL_CHANGE-2ff817bcfcdc80b89c32e00849a267a8"),
	RULES(null,"https://www.notion.so/RULES-2ff817bcfcdc802da4d2e80a6733f9ae"),
	PENALTY("/reservation/my-status","https://www.notion.so/PENALTY-2ff817bcfcdc809eb29cf6adb6a0d022"),
	FACILITY(null,"https://www.notion.so/FACILITY-2ff817bcfcdc80309ba7f92fb7f31a48"),
	ETC("/adminpage","https://www.notion.so/ETC-2ff817bcfcdc8056af0dc0648c18fbb1");

	// 2. enum 안의 필드 선언
	private final String route;
	private final String notionUrl;

	// 3. 생성자
	CategoryType(String route, String notionUrl) {
		this.route = route;
		this.notionUrl = notionUrl;
	}

	// 4. getter 메서드
	public String getRoute(){
		return route;
	}
	public String getNotionUrl(){
		return notionUrl;
	}

}

