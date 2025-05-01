from requests.auth import HTTPBasicAuth
import requests
import json
import apihelpers
import os
import fileinput
import datetime
import csv 

apikey = 'key goes here' #requires API key from app

filename = "C:\\blmgDataService\\Loaves\\MassDelete\\delIndividuals\\wGifts-remainder.txt"

print("Mass Delete - processing file: ",filename)

print("Start time:",datetime.datetime.now())
f = open(filename, "r")

counter = 0
deleteCount = 0

f.readline() # skip the first line - header

for line in f: #loop starts reading on 2nd line
    parts = line.split("\t")
    counter += 1

    acctNumber = (parts[0])
    lookupName = (parts[1])
    lookupName = lookupName.strip("\"")
    id = 0
    cust = ""
    transList = []

    #TODO: Pukes on records with CR in Address - need to handle acctnumber lookup fail gracefully
    
    print("====")
    print ("Getting account:",acctNumber,"::",lookupName)
    id = apihelpers.getIdForAcctNumber(acctNumber,apikey)
    print("AcctNumber",acctNumber,"Id:",id)

    if (id == -1):
        print ("\t ** Account not found - moving on")
        continue

    # wouldn't have to get the full constituent record, but we want to check name and household info
    response = apihelpers.getConstitById(id,apikey)
    cust = json.loads(response)
    #print(cust)
    print ("\t",cust["AccountNumber"],cust["Id"],cust["FullName"],cust["FirstName"],cust["LastName"],cust["IsInHousehold"])

    #do some safety checking before deleting
    if (cust["IsInHousehold"]):
        print ("this record is in a household - will not delete")
        continue
    elif (cust["FullName"] != lookupName):
        print ("name mismatch - won't delete")
        continue
    else:
        print ("\t its a match! getting transactions for",cust["Id"])
        response = apihelpers.getTransactions(id,apikey)
        transList = json.loads(response)
        print ("\t",transList["ResultCount"],"transactions")
        if (transList["ResultCount"] == 0):
            print ("no transactions")
        # remove comments if you want to save records with >5 transactions
        #elif (transList["ResultCount"] >5 ):
        #    print ("More than 5 transactions - moving on")
        #    continue
        else:
            print ("\t will delete transactions first")
            print (transList)
            print ("\tneed to delete these transactions")

            for tr in transList["Results"]:
                print ("\tTransId",tr["Id"])
                responseStatus = apihelpers.deleteTransId(tr["Id"],apikey)

                if (responseStatus == 200):
                    print()
                else:
                    print("WARNING - Transaction Delete NOT successful - moving to next record")
                    continue

        # if you make it this far - delete the constituent
        print ("\tattempting to delete Constituent id",cust["Id"])
        apihelpers.deleteConstitId (id, apikey)
        deleteCount +=1


f.close()
print("====")
print("End time:",datetime.datetime.now())
print("Records processed:",counter,"Deletes attempted:",deleteCount)

