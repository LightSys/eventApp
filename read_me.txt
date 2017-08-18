JSON file:
	DATE: mm/dd/yyyy hh:mm:ss
	NOTIFICATIONS: names must be digits and unique (is this still true?)
	CONTACTS PAGE ID: 0=textview, 1=listview (list items separated by ':'
	NAVIGATION: if "nav is null for a page it will not be imported and page will not be displayed in nav menu.
	GENERAL: titles must be "refresh", "year", "welcome_message", and "logo"
		refresh must be in minutes
		icon - must be in base64 format. if unavailable set value as "null"
	THEME: "theme..." colors can be valid colors or set as "null" for default theme colors
		"theme1, theme2, and theme3 can be used if logo doesn't display properly over theme colors in. navigation header.  
		"schedule..." color names can be changed as long as they match Schedule item categories
	SCHEDULE: days must be MM/dd/yyyy format 
		start_time: based on 24-hour clock. can be set to any time, but schedule items may not overlap
		length: in minutes
		description: text will show in schedule
		location: if available links to directions (must match contact name exactly), may be set to null
		category: must match a specified color
		   

	INFORMATION PAGES: "~" will be replaced with bullet arrow
	
	
	AVAILABLE TITLE ICONS: 
		"ic_contact" - 
		"ic_schedule" - 
		"ic_info" - 
		"ic_mountains" - 
		"ic_house" - 
		"ic_group: - 
		"ic_bell" -
		"ic_refresh" - 
		"ic_camera" -
		"ic_clipboard" -  

APP:

	IMPORT:
		- imports data from a JSON file (?see JSON example for format)
		ON INSTALL/RESCAN BUTTON: asks for QR code to retrieve data
		ON REFRESH BUTTON: uses previous QR code to retrieve data
		AUTOUPDATE: checks for new notifications. if notifications has "refresh": 		true, entire database will be refreshed (?untested)
		ERROR: gives options to 1. retry connecting using old QR code 2. rescan QR 		3. cancel and use old data
	NOTIFICATIONS PAGE:
	CONTACTS PAGE:
	SCHEDULE PAGE:
	HOUSING PAGE:
	PRAYER PARTNERS PAGE:
	INFORMATION PAGES:
	

		