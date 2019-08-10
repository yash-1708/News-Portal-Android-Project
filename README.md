# News-Portal-Android-Project
Complete 2 App System for the News Portal of a College created for Android with a Backend in Google's Firebase Database

*IMPORTANT*

*For admin login use username and password as 'test'*

*Sign in with any gmail login to bypass the email login step*

This was an android project created by me as a mini project for my college.
The project consists of two android applications, but since I couldn't publish both of them in the same repository, I have given the link to the user app's repository below
https://github.com/yash-1708/News-Portal_Android-Project-User-App

OVERVIEW & WORKING:

-This is a news portal for a college, it intends to help in the efficient and effortless transfer of news within a college.
 There are two apps, one for the admin and one for the user.
The Admin app is supposed to be given to professors and the user app is supposed to be given to the students

-Students can see published news on the app and submit their own news that they see everyday and these submissions will be stored in the database as unapproved news and will not be visible to the general users but will only be visible to the admins.

-The job of the admins is to edit,approve or delete these news and submit news of their own if any.
The news submitted by students will only be published in the user apps after an admin has approved of them to avoid spread of prank news and misinformation.

-The database is implemented in firebase and has Node.js functions in it to send automatic notifications to the users when a new news is published.

-The submission interface allows you to store an image related to the news, a title, a body of the news and select a section in which the news may be published.

-Unique Login System to curb potential pranks

-Admins can add other admins

-Admins can see unapproved news highlighted in red which they may then edit and/or approve and approved news highlighted in green which they may then edit or delete(One click on edit button in admin app makes news editable and another click on same button updates editedd news in database.)

-Users (students) are only allowed to view approved news and submit news
