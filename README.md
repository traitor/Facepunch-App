Description
---------------------

This is the official repository for the Facepunch for Android app. It is publicly available so that users can optionally customize the application for their specific needs and because this repository also houses the Facepunch Java API. While the code is free for personal use, only the API code (APISession.java) may be used in an application that is intended to be distributed publicly.

API details
---------------------

The API code, located in `src/nl/vertinode/facepunch/APISession.java`, is completely standalone and does not depend on an external server. That means that requests use the same amount of bandwidth as simply opening the page with the data in your browser. This approach has been selected for a multitude of reasons listed below.

**Advantages**

+ No additional reliance on an external server that could possibly go down.
+ No possibility of abusing an external server and getting it banned.
+ No direct possibility of eavesdropping.

**Disadvantages**

+ Uses significantly more bandwidth than required (up to 10 times as much).
+ Change in layout of the original website requires the API to be updated clientside instead of a server patch.

Because safe account access is extremely important and layout changes are infrequent, the advantages easily outweigh the disadvantages. The bandwidth usage could still pose a problem on mobile connections, but it still uses less bandwidth than the mobile browser would use, because images, stylesheets and other external files are not downloaded.

Roadmap
---------------------

**Version 1**

+ *Login page* - Saves user details, layout with basic login form.
+ *Frontpage* - List of main categories with top level subforums, displayed with icons and titles, possibly descriptions. Displayed forums depends on logged in user.
+ *Forum* - Displays subforums and threads in current subforum. Shows thread icons, titles, author, last post and total amount of posts.
+ *Thread* - Displays posts with user details and post contents. Avatar, username with indication of rank, postcount and possibly joindate are shown for the user. For the post contents text formatting, quotes, release tags, images and smilies are supported, images will be loaded on-demand in an external view. Quotes inside quotes and tables don't have to be supported. The ratings are shown at the bottom of a post. A post can be rated or replied to by holding your finger down on it.
+ *Post* - Displays a post input box with buttons that help with adding images, text formatting, etc. very much like the text editor on the site. Also includes a visual preview. It should allow you to embed images on your phone by automatically uploading them to imgur.com and inserting the url in the post.
+ In the forum and thread view, there should also be controls to view different pages.

Screenshots
---------------------

![Login form](http://i.imgur.com/m8v58.png)