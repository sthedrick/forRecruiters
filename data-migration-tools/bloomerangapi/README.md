This directory Includes Sample code from methods used to Mass Update and delete data for Loaves and Fishes food pantry Donor database (Bloomerang API)

Technology: python

- apihelpers.py - helper methods to retrieve, update values using API
- addConstit.py - sample code to add a constituent record
- updateConstit.py - lookup client and update basic information
- createConstitList.py - create a list of consituents including hyperlink to their direct page
- massDeleteYesGifts-Loaves.py - lookup clents, remove transactions, then remove client record
	

Sample data files
- For the sample, names were adjusted, addresses removed, dates and amounts replaced with random values.
	delHHPart1.txt
	delHHPart7-2gifts.txt
	delHHRemainder.txt
	wGifts-remainder.txt

Note: Some methods required curl from within python, others did not
