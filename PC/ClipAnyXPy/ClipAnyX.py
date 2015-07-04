#!/usr/bin/python
# -*- coding: UTF-8 -*
'''
Created on 2015年7月2日

@author: RobinTang
'''

from Bmob import BmobSDK, BmobModel

BmobSDK.setup('cf7c391e0adea90eb0855f516ee78590', '63ee015e07cf8d04e3d4ce133da951db')

class TextData(BmobModel):
	"""docstring for TextData"""
	text = ''
	uid = ''


def usage():
	print 'Usage:\n\tload from cloud: ClipAnyX -u uid\n\tsave to cloud: ClipAnyX -u uid -t yourtext'


# if you want to use this tool, the follows function must be implement.
clip = ''
def getClip():
	'''
	get text from clipboard
	'''
	global clip
	return clip
def setClip(text):
	'''
	set text to clipboard
	'''
	global clip
	clip = text

def main():
	import getopt, sys, time
	try:
		opts, args = getopt.getopt(sys.argv[1:], "hu:t:i:vd",['help', 'uid', 'text','interval'])
	except getopt.GetoptError as err:
		# print help information and exit:
		print str(err) # will print something like "option -a not recognized"
		usage()
		sys.exit(2)

	uid = None
	text = None
	interval = 10
	daemon = False
	for o, a in opts:
		if o == "-v":
			print '1.0'
		elif o in ("-h", "--help"):
			usage()
			sys.exit()
		elif o in ('-u','--uid'):
			uid = a
		elif o in ('-t','--text'):
			text = a
		elif o in ('-i','--interval'):
			interval = int(a)
		elif o == '-d':
			daemon = True
		else:
			assert False, "unhandled option: %s"%o

	if not uid:
		print 'error, need uid!'
		usage()
		sys.exit()

	t = TextData(uid=uid, text=text)
	if text:
		t.save()
		print text
	else:
		nt = t.query().w_eq('uid', uid).order('-createdAt').first()
		if nt:
			setClip(nt.text)
			print nt.text

	rawcurclip = getClip()
	while daemon:
		# check local
		curclip = getClip()
		if rawcurclip != curclip:
			# clip changed
			rawcurclip == curclip
			nt = TextData(uid=uid, text=curclip)
			nt.save()
			print 'Save:%s'%curclip

		# check cloud
		nt = t.query().w_eq('uid', uid).order('-createdAt').first()
		if nt and nt.text != curclip:
			rawcurclip = curclip = nt.text
			setClip(curclip)
			print 'Load:%s'%curclip

		time.sleep(interval)

if __name__ == '__main__':
	main()

