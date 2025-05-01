from requests.auth import HTTPBasicAuth
import requests
import json
import apihelpers
import os

apikey = 'key removed' # requires key from APP

acctNumber = "1228" #Joe Test
fieldIdToUpdate = 13314 #Volunteer note (want to get this from a map)

acctid = apihelpers.getIdForAcctNumber(acctNumber, apikey)

print (acctid)

if (acctid <= 0):
    print ("error",acctid)
    exit
else:
    results = apihelpers.getConstitById(acctid, apikey)
    #print ("return result type",type(results))
    #print ("return result",results)

account = json.loads(results)

print (account["AccountNumber"],account["Id"],account["Type"],account["FirstName"],account["LastName"])

# if you want to see list of all the current object info
##    for key in account:
##        print("\t",key,"\t",account[key])
    
valueList = (account["CustomValues"])
##
##    for val in valueList:
##        print (val)

###test list of values
##for key in account:
##    print("\t",key,"\t",account[key])

#ready to post

body = {
    "Type": account["Type"],
    "FirstName": account["FirstName"], 
    "LastName": account["LastName"], 
    "CustomValues":[
        {
            "FieldId":fieldIdToUpdate #Volunteer note
            ,"Value":"Updated volunteer note - again"
        },
        {   "FieldId":13313 #Availability
            #,"ValueIds": [15366,14343] #Friday, Saturday
            ,"ValueIds": [] #empty
        }
    ]
}

print ("ready to post")

url = "https://api.bloomerang.co/v2/constituent/" + str(acctid)

headers = {'Accept': 'application/json',
           'X-API-KEY': apikey}

print (url)

response = requests.put(url, headers=headers,json=body)
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

