import sys
import csv
import io
import json
import time
import datetime
import numpy as np
import collections

USER_ID_INDEX = 1
SOURCE_COL_INDEX = 10
UNIT_TEST_COL_INDEX = 9
TIME_STAMP_COL_INDEX = 5
ATTEMPT_COL_INDEX = 7
TIME_COL_INDEX = 8
PASSING = 20
PERFECT = 100
TEST_USER_ID = '4448'
AST_DIR = 'unique'

def noteUnitTest(assnId):
	dataDirPath = '../' + assnId + '/'
	dumpFile = open(dataDirPath + 'activitiesDump.csv')
	loaded = 0

	# maps from userIds to a all of that users entries...
	userMap = {}

	for line in dumpFile:
		row = line.split(',')

		userId = stripQuotes(row[USER_ID_INDEX])

		if not userId in userMap:
			userMap[userId] = []
		userMap[userId].append(row)

		loaded += 1

	idMap = loadIdMap(dataDirPath)
	saveTrajectories(dataDirPath, userMap, idMap)

def saveTrajectories(dataDirPath, userMap, idMap):
	for userId in userMap:
		entries = sortEntries(userMap[userId])
		firstEntry = entries[0]
		startTime = getLinuxTime(firstEntry)
		userFile = open(dataDirPath + 'trajectories/' + userId + '.txt', 'w')
		for entry in entries:
			linuxTime = getLinuxTime(entry)
			timeFromStart = linuxTime - startTime
			uniqueId = getUniqueId(entry, idMap)
			if uniqueId == None: break
			userFile.write(uniqueId + ', '  + str(timeFromStart) + '\n')
			unitTestResult = getUnitTestResult(entry)
			if unitTestResult >= PERFECT:
				break

def getUniqueId(row, idMap):
	sourceId = stripQuotes(row[SOURCE_COL_INDEX])
	if not sourceId in idMap:
		print 'source id: ' + sourceId + ' not found'
		return None
	return idMap[sourceId]

def getLinuxTime(row):
	timeStamp = stripQuotes(row[TIME_STAMP_COL_INDEX])
	# example 2013-12-13 16:10:20
	timePattern = "%Y-%m-%d %H:%M:%S"
	strpTime = datetime.datetime.strptime(timeStamp, timePattern)
	return time.mktime(strpTime.timetuple())

def getUnitTestResult(row):
	return int(stripQuotes(row[UNIT_TEST_COL_INDEX]))

def sortEntries(entries):
	attemptMap = {}
	for row in entries:
		attempt = int(stripQuotes(row[ATTEMPT_COL_INDEX]))
		attemptMap[attempt] = row
	orderedMap = collections.OrderedDict(sorted(attemptMap.items()))
	ordered = []
	for key in orderedMap:
		ordered.append(attemptMap[key])
	return ordered


def testEntries(entries):
	lastAttempt = -1
	for row in entries:
		attempt = int(stripQuotes(row[ATTEMPT_COL_INDEX]))
		if attempt < lastAttempt:
			raise Exception('not monotonic')
		lastAttempt = attempt

def loadIdMap(dataDirPath):
	idMap = {}
	idMapFile = open(dataDirPath + AST_DIR +'/idMap.txt')
	for line in idMapFile:
		row = line.strip().split(',')
		oldId = row[0]
		newId = row[1]
		idMap[oldId] = newId
	return idMap

def stripQuotes(toStrip):
	strLen = len(toStrip)
	return toStrip[1:strLen-1]

if __name__ == "__main__":
	if len(sys.argv) != 2:
		raise Exception('usage: noteUnitTest.py <assnId>')
	assnId = sys.argv[1]
	noteUnitTest(assnId)