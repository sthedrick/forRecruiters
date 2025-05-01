from requests.auth import HTTPBasicAuth
import requests
import json
import os

def getIdForAcctNumber(acctNumber, apikey):
    
    id = -1
    skip = "0"
    take = "5"
    searchString = str(acctNumber)
        
    ##without constitSearchType - placing name in front for safety of quotes
    #curl -X GET "https://api.bloomerang.co/v2/constituents/search?search="Barnes"&skip=0&take=50" -H "accept: application/json" -H "X-API-KEY: 82ee521c-dc42-b57f-6898-ff86b6dac848"
    #getstring = 'curl -X GET "https://api.bloomerang.co/v2/constituents/search?search="' + searchString + '"&skip='+ skip + '&take=' + take + '" -H "accept: application/json" -H "X-API-KEY: ' + apikey + '"'

    getstring = 'curl -X GET "https://api.bloomerang.co/v2/constituents/search?search="' + searchString + '"&skip='+ skip + '&take=' + take + '" -H "accept: application/json" -H "X-API-KEY: ' + apikey + '"'
    #print (getstring)  #for debug

    response = os.popen(getstring).read()
    #print (response)   #for debug

    response_json = json.loads(response)
    result_count = response_json['ResultCount']
    #print (result_count, "results found")
    results = response_json["Results"]

    if (result_count == 1):
        id = results[0]["Id"]

        
    elif (result_count) == 0:
        print ("Error: No results returned for Account Number",acctNumber)
    else:
        #trouble - print the extra stuff
        print ("Error - more than one result for search on " + searchString)

        for account in results:
            # you may not want to print this list - or put a optional parm
            print (account["AccountNumber"],account["Id"],account["FullName"])
            if (account["Type"] == "Household"):
                print ("This is a household with the following members:")
                for mid in account["MemberIds"]:
                    print ("\t",mid)

    
    return id


def deleteConstitId (idToDelete, apikey):
    ##danger - this is quick and deadly - Don't forget it's by ID NOT account number!!
    ## it will not let you delete a constituent with transactions
    
    url = "https://api.bloomerang.co/v2/constituent/" + str(idToDelete)
    headers = {'Accept': 'application/json',
               'X-API-KEY': apikey}
      
    response = requests.delete(url, headers=headers)
    # print(response)

    # hoping for "<Response [200]>"
    
    if (response.status_code == 200):
        print("Constituent Delete successful - ID:",idToDelete)
        print("\t",response.content)
    else:
        print("WARNING - Constituent Delete NOT successful")
        print(response)
        print(response.content)

    return response.status_code
    """
    #<Response [200]>
    #b'{"Id":1990657,"Type":"Constituent","Deleted":true}'

    #<Response [404]>
    #b'{\r\n  "Message": "No constituent with id 1990657 found",\r\n  "ErrorCode": 202\r\n}'

    <Response [400]>
    b'{\r\n  "Message": "Cannot delete constituent because it has transactions.",\r\n  "ErrorCode": 404\r\n}'
    """

def deleteTransId (idToDelete, apikey):
    ##danger - this is quick and deadly - Don't forget it's by transaction ID
    ## it will not let you delete a constituent with transactions

    url = "https://api.bloomerang.co/v2/transaction/" + str(idToDelete)
    headers = {'Accept': 'application/json',
               'X-API-KEY': apikey}
      
    response = requests.delete(url, headers=headers)

    # hoping for "<Response [200]>"
    
    if (response.status_code == 200):
        print("Transaction Delete successful - ID:",idToDelete)
        print("\t",response.content)
    else:
        print("WARNING - Transaction Delete NOT successful")
        print(response)
        print(response.content)

    return response.status_code

def getHHById(householdId, apikey):
    # expects acctNumber to be a string
            
    #curl -X GET "https://api.bloomerang.co/v2/household/603140" -H "accept: application/json" -H "X-API-KEY: 'apikeyhere'"
    searchString = str(householdId)
    
    getstring = 'curl -X GET "https://api.bloomerang.co/v2/household/' + searchString + '" -H "accept: application/json" -H "X-API-KEY: ' + apikey + '"'
    #print (getstring)  #for debug

    response = os.popen(getstring).read()

    #print ("response",response) #for debug

    
    
    return response

def getConstitById(constitid, apikey):
    # expects acctNumber to be a string
            
    #curl -X GET "https://api.bloomerang.co/v2/constituent/603140" -H "accept: application/json" -H "X-API-KEY: 'apikeyhere'"
    searchString = str(constitid)
    
    getstring = 'curl -X GET "https://api.bloomerang.co/v2/constituent/' + searchString + '" -H "accept: application/json" -H "X-API-KEY: ' + apikey + '"'
    #print (getstring)  #for debug

    response = os.popen(getstring).read()

    #print ("response",response) #for debug

    
    
    return response

def getTransactions(constitid, apikey):
    # expects constitid to be a string
    # gets the first 50 transactions - that's the API default - not sure what the max is

    # TODO - check and complain if there are more than 50 and you didn't get them all
    
    id = -1
    skip = "0"
    take = "50" # default value is 50
           
    #curl -X GET "https://api.bloomerang.co/v2/transactions?skip=0&take=50&accountId=12345" -H "accept: text/json" -H "X-API-KEY: apikeyhere'"

    getstring = 'curl -X GET "https://api.bloomerang.co/v2/transactions?skip='+ skip + '&take=' + take + '&accountId=' + str(constitid) + '" -H "accept: application/json" -H "X-API-KEY: ' + apikey + '"'
    #print (getstring)  #for debug

    response = os.popen(getstring).read()

    #print ("response",response) #for debug

    
    
    return response

def getCustomFieldList(fieldType, apikey):

    #valid values are Constituent, Transaction, Interaction, Note, Benevon
    # API also allows you to specify isActive (boolean) but it's not implemented in this method
    
    if (fieldType == ""):
        fieldType = "Constituent"

    url = "https://api.bloomerang.co/v2/customFields/" + fieldType
    headers = {'Accept': 'application/json',
               'X-API-KEY': apikey}
      
    response = requests.get(url, headers=headers)
    
    # error checking
    if (response.status_code == 200):
        print("Find successful")
        
    else:
        print("WARNING - List fields request NOT successful")
        print (response)
        print(response.content)

    return response.content
