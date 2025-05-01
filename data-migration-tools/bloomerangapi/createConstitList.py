from requests.auth import HTTPBasicAuth
import requests
import json
import apihelpers
import os

apikey = 'removed' #Loaves

f = open("constitList.txt", "a")
f.write("Appending lines to the file")

# not passing an account number - we want them all
id = -1
skip = "0"
take = "50"

#getstring = 'curl -X GET "https://api.bloomerang.co/v2/constituents/search?search="' + searchString + '"&skip='+ skip + '&take=' + take + '" -H "accept: application/json" -H "X-API-KEY: ' + apikey + '"'

counter = 0
while counter < 60000:
    #here we want everyone
    getstring = 'curl -X GET "https://api.bloomerang.co/v2/constituents?skip='+ str(counter) + '&take=' + take + '" -H "accept: application/json" -H "X-API-KEY: ' + apikey + '"'
    #print (getstring)  #for debug

    response = os.popen(getstring).read()
    #print (response)   #for debug

    response_json = json.loads(response)

    result_count = response_json['ResultCount']
    #print (result_count, "results found")
    results = response_json["Results"]

    for account in results:
        # you may not want to print this list - or put a optional parm

        
        webaddress = "https://crm.bloomerang.co/Constituent/" + str(account["Id"])
        outputstring = str(account["AccountNumber"]) + "\t" + account["FullName"] + "\t" + webaddress
        print (outputstring)
    ##    if (account["Type"] == "Household"):
    ##        print ("This is a household with the following members:")
    ##        for mid in account["MemberIds"]:
    ##            print ("\t",mid)

    counter +=50

f.close()
