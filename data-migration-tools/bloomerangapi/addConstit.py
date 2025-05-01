from requests.auth import HTTPBasicAuth
import requests
import json
import apihelpers
import os

apikey = 'key removed from here'

url = "https://api.bloomerang.co/v2/constituent"

headers = {'Accept': 'application/json',
           'X-API-KEY': apikey}
body = {
  "Type": "Individual",
  "Status": "Active",
  "FirstName": "string",
  "LastName": "string",
  "MiddleName": "string",
  "Prefix": "Mr.", #Must match a prefix in the Bloomerang CRM
  "Suffix": "III", #Must match a prefix in the Bloomerang CRM
  #"FullName": "string", "Message": "Cannot set FullName for individuals"
  "InformalName": "string",
  "FormalName": "string",
  "EnvelopeName": "string",
  "RecognitionName": "string"
  }
  
response = requests.post(url, headers=headers,json=body)
print(response.content)

"""
b'{"Id":1990657,"AccountNumber":1219,"CommunicationRestrictions":[],
"EmailInterestType":"All","DonorSearchInfo":{"GenerosityScore":"NotScanned"},
"IsFavorite":false,"IsInHousehold":false,"IsHeadOfHousehold":false,"Type":"Individual",
"Status":"Active","FirstName":"string","LastName":"string","MiddleName":"string","Prefix":"Mr.","Suffix":"III",
"FullName":"string string string III","InformalName":"string","FormalName":"string",
"EnvelopeName":"string","RecognitionName":"string","Website":"","FacebookId":"","TwitterId":"","LinkedInId":"",
"Gender":"","ProfilePictureType":"None","EngagementScore":"Low","AddressIds":[],"EmailIds":[],"PhoneIds":[],
"CustomValues":[],
"AuditTrail":{"CreatedDate":"2023-03-27T20:01:50Z","CreatedName":"hedrickHelp-Scott Hedrick","LastModifiedDate":"2023-03-27T20:01:50Z","LastModifiedName":"hedrickHelp-Scott Hedrick"}}'
"""

"""
ConstituentBase{
Type	ConstituentTypestring [ Individual, Organization ]
Enum:
Array [ 2 ]
Status	AccountStatusstring
Enum:
Array [ 3 ]
FirstName	string
LastName	string
MiddleName	string
Prefix	        string   Must match a prefix in the Bloomerang CRM
Suffix          string   Must match a suffix in the Bloomerang CRM
FullName	string  (Only for organization)
InformalName	string
FormalName	string
EnvelopeName	string
RecognitionName	string
JobTitle	string
Employer	string
Website	string($uri)
FacebookId	string
TwitterId	string
LinkedInId	string
Gender	        Genderstring
Enum:
Array [ 3 ]
Birthdate	string($date)
ProfilePictureType	ProfilePictureTypestring
Enum:
Array [ 3 ]
PrimaryEmail	Email{...}
PrimaryPhone	Phone{...}
}
"""

