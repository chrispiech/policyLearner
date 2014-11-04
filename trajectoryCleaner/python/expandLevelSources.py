import sys
import csv
import io
import json

def expand(assnId):
	dumpFile = open('../' + assnId + '/sourcesDump.csv')
	for line in dumpFile:
		row = line.split(',')
		xml = stripQuotes(row[3])
		xml = replaceUnquote(xml)
		sourceId = stripQuotes(row[0])
		saveXml(assnId, sourceId, xml)

def saveXml(assnId, sourceId, xml):
	path = '../' + assnId + '/xml/' + sourceId + '.xml'
	outFile = open(path, 'w')
	outFile.write(xml)
	print path

def stripQuotes(toStrip):
	strLen = len(toStrip)
	return toStrip[1:strLen-1]

def replaceUnquote(toReplace):
	return toReplace.replace('\\"', '"')

if __name__ == "__main__":
	if len(sys.argv) != 2:
		raise Exception('usage: expandLevelSources.py <assnId>')
	assnId = sys.argv[1]
	expand(assnId)