/*
Copyright (c) 2007-2010, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package twitter4j;

import twitter4j.json.DataObjectFactory;

import static twitter4j.DAOTest.assertDeserializedFormIsEqual;
import static twitter4j.DAOTest.assertDeserializedFormIsNotEqual;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.List;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public class TwitterTest extends TwitterTestBase {

    public TwitterTest(String name) {
        super(name);
    }


    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetPublicTimeline() throws Exception {
        List<Status> statuses;
        statuses = twitter1.getPublicTimeline();
        assertTrue("size", 0 < statuses.size());
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
    }

    public void testGetHomeTimeline() throws Exception {
        List<Status> statuses = twitter1.getHomeTimeline();
        assertTrue(0 < statuses.size());
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
    }
    public void testSerializability() throws Exception {
        Twitter twitter = new TwitterFactory().getInstance("foo", "bar");
        Twitter deserialized = (Twitter)assertDeserializedFormIsEqual(twitter);

        assertEquals(deserialized.getScreenName(), twitter.getScreenName());
        assertEquals(deserialized.auth, twitter.auth);

        twitter = new TwitterFactory().getInstance();
        deserialized = (Twitter)assertDeserializedFormIsEqual(twitter);
        assertEquals(deserialized.auth, twitter.auth);
    }
    public void testGetScreenName() throws Exception {
        assertEquals(id1.screenName, twitter1.getScreenName());
        assertEquals(id1.id, twitter1.getId());
    }

    public void testGetFriendsTimeline() throws Exception {
        List<Status> actualReturn;

        actualReturn = twitter1.getFriendsTimeline();
        assertTrue(actualReturn.size() > 0);
        actualReturn = twitter1.getFriendsTimeline(new Paging(1l));
        assertTrue(actualReturn.size() > 0);
        //this is necessary because the twitter server's clock tends to delay
        actualReturn = twitter1.getFriendsTimeline(new Paging(1000l));
        assertTrue(actualReturn.size() > 0);

        actualReturn = twitter1.getFriendsTimeline(new Paging(1));
        assertTrue(actualReturn.size() > 0);
        assertNotNull(DataObjectFactory.getRawJSON(actualReturn));
        assertEquals(actualReturn.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(actualReturn.get(0))));
    }

    public void testShowUser() throws Exception {
        User user = twitter1.showUser(id1.screenName);
        assertEquals(id1.screenName, user.getScreenName());
        assertNotNull(user.getLocation());
        assertNotNull(user.getDescription());
        assertNotNull(user.getProfileImageURL());
        assertNotNull(user.getURL());
        assertFalse(user.isProtected());

        assertTrue(0 <= user.getFavouritesCount());
        assertTrue(0 <= user.getFollowersCount());
        assertTrue(0 <= user.getFriendsCount());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getTimeZone());
        assertNotNull(user.getProfileBackgroundImageUrl());

        assertTrue(0 <= user.getStatusesCount());
        assertNotNull(user.getProfileBackgroundColor());
        assertNotNull(user.getProfileTextColor());
        assertNotNull(user.getProfileLinkColor());
        assertNotNull(user.getProfileSidebarBorderColor());
        assertNotNull(user.getProfileSidebarFillColor());
        assertNotNull(user.getProfileTextColor());

        assertTrue(1 < user.getFollowersCount());
        if (null != user.getStatus()) {
            assertNotNull(user.getStatusCreatedAt());
            assertNotNull(user.getStatusText());
            assertNotNull(user.getStatusSource());
            assertFalse(user.isStatusFavorited());
            assertEquals(-1, user.getStatusInReplyToStatusId());
            assertEquals(-1, user.getStatusInReplyToUserId());
            assertFalse(user.isStatusFavorited());
            assertNull(user.getStatusInReplyToScreenName());
        }

        assertTrue(1 <= user.getListedCount());
        assertFalse(user.isFollowRequestSent());

        //test case for TFJ-91 null pointer exception getting user detail on users with no statuses
        //http://yusuke.homeip.net/jira/browse/TFJ-91
        twitter1.showUser("twit4jnoupdate");
        user = twitter1.showUser("tigertest");
        User previousUser = user;
        assertNotNull(DataObjectFactory.getRawJSON(user));

        user = twitter1.showUser(numberId);
        assertEquals(numberIdId, user.getId());
        assertNull(DataObjectFactory.getRawJSON(previousUser));
        assertNotNull(DataObjectFactory.getRawJSON(user));
        assertEquals(user, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(user)));

        previousUser = user;
        user = twitter1.showUser(numberIdId);
        assertEquals(numberIdId, user.getId());
        assertNotNull(DataObjectFactory.getRawJSON(user));
        assertEquals(user, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(user)));
    }

    public void testLookupUsers() throws TwitterException {
        ResponseList<User> users = twitter1.lookupUsers(new String[]{id1.screenName, id2.screenName});
        assertEquals(2, users.size());
        assertContains(users, id1);
        assertContains(users, id2);

        users = twitter1.lookupUsers(new int[]{id1.id, id2.id});
        assertEquals(2, users.size());
        assertContains(users, id1);
        assertContains(users, id2);
        assertNull(users.getFeatureSpecificRateLimitStatus());
        assertNotNull(DataObjectFactory.getRawJSON(users.get(0)));
        assertEquals(users.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(users.get(0))));
        assertNotNull(DataObjectFactory.getRawJSON(users));
    }

    private void assertContains(ResponseList<User> users, TestUserInfo user) {
        boolean found = false;
        for(User aUser : users){
            if(aUser.getId() == user.id && aUser.getScreenName().equals(user.screenName)){
                found = true;
                break;
            }
        }
        if(!found){
            fail(user.screenName + " not found in the result.");
        }

    }

    public void testSearchUser() throws TwitterException {
        ResponseList<User> users = twitter1.searchUsers("Doug Williams",1);
        assertTrue(4 < users.size());
        assertNotNull(users.getFeatureSpecificRateLimitStatus());
        assertNotNull(DataObjectFactory.getRawJSON(users.get(0)));
        assertEquals(users.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(users.get(0))));
        assertNotNull(DataObjectFactory.getRawJSON(users));
    }
    public void testSuggestion() throws Exception {
        ResponseList<Category> categories = twitter1.getSuggestedUserCategories();
        assertTrue(categories.size() > 0);
        assertNotNull(DataObjectFactory.getRawJSON(categories));
        assertNotNull(DataObjectFactory.getRawJSON(categories.get(0)));
        assertEquals(categories.get(0), DataObjectFactory.createCategory(DataObjectFactory.getRawJSON(categories.get(0))));
        ResponseList<User> users = twitter1.getUserSuggestions(categories.get(0).getSlug());
        assertTrue(users.size() > 0);
        assertNotNull(DataObjectFactory.getRawJSON(users));
        assertNotNull(DataObjectFactory.getRawJSON(users.get(0)));
        assertEquals(users.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(users.get(0))));
    }
    public void testProfileImage() throws Exception {
        ProfileImage image = twitter1.getProfileImage(id1.screenName, ProfileImage.BIGGER);
        assertNotNull(image.getURL());
    }


    // list deletion doesn't work now.
    // http://groups.google.com/group/twitter-development-talk/t/4e4164a347da1c3b
    // http://code.google.com/p/twitter-api/issues/detail?id=1327
    public void testList() throws Exception {
        PagableResponseList<UserList> userLists;
        userLists = twitter1.getUserLists(id1.screenName,-1l);
        assertNotNull(DataObjectFactory.getRawJSON(userLists));
        for(UserList alist : userLists){
            twitter1.destroyUserList(alist.getId());
        }

        /*List Methods*/
        UserList userList;
        userList = twitter1.createUserList("testpoint1", false, "description1");
        assertNotNull(DataObjectFactory.getRawJSON(userList));
        assertEquals(userList, DataObjectFactory.createUserList(DataObjectFactory.getRawJSON(userList)));
        assertNotNull(userList);
        assertEquals("testpoint1", userList.getName());
        assertEquals("description1", userList.getDescription());

        userList = twitter1.updateUserList(userList.getId(), "testpoint2", true, "description2");
        assertEquals(userList, DataObjectFactory.createUserList(DataObjectFactory.getRawJSON(userList)));
        assertTrue(userList.isPublic());
        assertNotNull(DataObjectFactory.getRawJSON(userList));
        assertNotNull(userList);
        assertEquals("testpoint2", userList.getName());
        assertEquals("description2", userList.getDescription());


        userLists = twitter1.getUserLists(id1.screenName, -1l);
        assertEquals(userList, DataObjectFactory.createUserList(DataObjectFactory.getRawJSON(userList)));
        assertNotNull(DataObjectFactory.getRawJSON(userList));
        assertFalse(userLists.size() == 0);

        userList = twitter1.showUserList(id1.screenName, userList.getId());
        assertEquals(userList, DataObjectFactory.createUserList(DataObjectFactory.getRawJSON(userList)));
        assertNotNull(DataObjectFactory.getRawJSON(userList));
        assertNotNull(userList);

        List<Status> statuses = twitter1.getUserListStatuses(id1.screenName, userList.getId(), new Paging());
        if (statuses.size() > 0) {
            assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        }
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertNull(DataObjectFactory.getRawJSON(userList));
        assertNotNull(statuses);

        /*List Member Methods*/
        User user = null;
        try {
            user = twitter1.checkUserListMembership(id1.screenName, id2.id, userList.getId());
            fail("id2 shouldn't be a member of the userList yet. expecting a TwitterException");
        } catch (TwitterException te) {
            assertEquals(404, te.getStatusCode());
        }
        userList = twitter1.addUserListMember(userList.getId(), id2.id);
        assertEquals(userList, DataObjectFactory.createUserList(DataObjectFactory.getRawJSON(userList)));
        assertNotNull(DataObjectFactory.getRawJSON(userList));
        userList = twitter1.addUserListMembers(userList.getId(), new int[]{id3.id,id2.id});
        assertEquals(userList, DataObjectFactory.createUserList(DataObjectFactory.getRawJSON(userList)));
        assertNotNull(DataObjectFactory.getRawJSON(userList));
        userList = twitter1.addUserListMembers(userList.getId(), new String[]{"akr", "yusukey"});
        assertEquals(userList, DataObjectFactory.createUserList(DataObjectFactory.getRawJSON(userList)));
        assertNotNull(DataObjectFactory.getRawJSON(userList));
        assertNotNull(userList);

        PagableResponseList<User> users = twitter1.getUserListMembers(id1.screenName, userList.getId(), -1);
        assertEquals(users.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(users.get(0))));
        assertNotNull(DataObjectFactory.getRawJSON(users));
        assertNull(DataObjectFactory.getRawJSON(userList));
        // workaround issue 1301
        // http://code.google.com/p/twitter-api/issues/detail?id=1301
//        assertEquals(userList.getMemberCount(), users.size());
        assertTrue(0 < users.size());// workaround issue 1301

        userList = twitter1.deleteUserListMember(userList.getId(), id2.id);
        assertNotNull(DataObjectFactory.getRawJSON(userList));
        assertEquals(userList, DataObjectFactory.createUserList(DataObjectFactory.getRawJSON(userList)));
        assertNotNull(userList);
        //
//        assertEquals(1, userList.getMemberCount());

        user = twitter1.checkUserListMembership(id1.screenName, userList.getId(), id3.id);
        assertNotNull(DataObjectFactory.getRawJSON(user));
        assertEquals(user, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(user)));
        assertEquals(id3.id, user.getId());

        userLists = twitter1.getUserListMemberships(id1.screenName, -1l);
        assertNotNull(DataObjectFactory.getRawJSON(userLists));
        assertEquals(userLists.get(0), DataObjectFactory.createUserList(DataObjectFactory.getRawJSON(userLists.get(0))));
        assertNotNull(userLists);

        userLists = twitter1.getUserListSubscriptions(id1.screenName, -1l);
        assertNotNull(DataObjectFactory.getRawJSON(userLists));
        if (userLists.size() > 0) {
            assertEquals(userLists.get(0), DataObjectFactory.createUserList(DataObjectFactory.getRawJSON(userLists.get(0))));
        }
        assertNotNull(userLists);
        assertEquals(0, userLists.size());

        /*List Subscribers Methods*/

        users = twitter1.getUserListSubscribers(id1.screenName, userList.getId(), -1);
        assertNotNull(DataObjectFactory.getRawJSON(users));
        assertEquals(0, users.size());
        try {
            twitter2.subscribeUserList(id1.screenName, userList.getId());
        } catch (TwitterException te) {
            // workarounding issue 1300
            // http://code.google.com/p/twitter-api/issues/detail?id=1300
            assertEquals(404,te.getStatusCode());
        }
        // expected subscribers: id2
        try {
            twitter3.subscribeUserList(id1.screenName, userList.getId());
        } catch (TwitterException te) {
            // workarounding issue 1300
            assertEquals(404, te.getStatusCode());
        }
        // expected subscribers: id2 and id4
        try {
            twitter2.unsubscribeUserList(id1.screenName, userList.getId());
        } catch (TwitterException te) {
            // workarounding issue 1300
            assertEquals(404, te.getStatusCode());
        }
        // expected subscribers: id4
        users = twitter1.getUserListSubscribers(id1.screenName, userList.getId(), -1);
//        assertEquals(1, users.size()); //only id4 should be subscribing the userList
        assertTrue(0 <= users.size()); // workarounding issue 1300
        try {
            user = twitter1.checkUserListSubscription(id1.screenName, userList.getId(), id3.id);
            assertNotNull(DataObjectFactory.getRawJSON(user));
            assertEquals(user, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(user)));
            assertEquals(id3.id, user.getId());
        } catch (TwitterException te) {
            // workarounding issue 1300
            assertEquals(404, te.getStatusCode());
        }

        userLists = twitter1.getUserListSubscriptions(id3.screenName, -1l);
        assertNotNull(userLists);
        if (userLists.size() > 0) {
            assertEquals(userLists.get(0), DataObjectFactory.createUserList(DataObjectFactory.getRawJSON(userLists.get(0))));
        }
//        assertEquals(1, userLists.size()); workarounding issue 1300

        try {
            user = twitter1.checkUserListSubscription(id1.screenName, id2.id, userList.getId());
            fail("id2 shouldn't be a subscriber the userList. expecting a TwitterException");
        } catch (TwitterException ignore) {
            assertEquals(404, ignore.getStatusCode());
        }

        userList = twitter1.destroyUserList(userList.getId());
        assertNotNull(DataObjectFactory.getRawJSON(userList));
        assertEquals(userList, DataObjectFactory.createUserList(DataObjectFactory.getRawJSON(userList)));
        assertNotNull(userList);
    }

    public void testUserTimeline() throws Exception {
        List<Status> statuses;
        statuses = twitter1.getUserTimeline();
        assertTrue("size", 0 < statuses.size());
        try {
            statuses = unauthenticated.getUserTimeline("1000");
            assertNotNull(DataObjectFactory.getRawJSON(statuses));
            assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
            assertTrue("size", 0 < statuses.size());
            assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
            assertEquals(9737332, statuses.get(0).getUser().getId());
            statuses = unauthenticated.getUserTimeline(1000);
            assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
            assertNotNull(DataObjectFactory.getRawJSON(statuses));
            assertTrue("size", 0 < statuses.size());
            assertEquals(1000, statuses.get(0).getUser().getId());

            statuses = unauthenticated.getUserTimeline(id1.screenName, new Paging().count(10));
            assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
            assertNotNull(DataObjectFactory.getRawJSON(statuses));
            assertTrue("size", 0 < statuses.size());
            statuses = unauthenticated.getUserTimeline(id1.screenName, new Paging(999383469l));
            assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
            assertNotNull(DataObjectFactory.getRawJSON(statuses));
            assertTrue("size", 0 < statuses.size());
        } catch (TwitterException te) {
            // is being rate limited
            assertEquals(400, te.getStatusCode());
        }

        statuses = twitter1.getUserTimeline(new Paging(999383469l));
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertTrue("size", 0 < statuses.size());
        statuses = twitter1.getUserTimeline(new Paging(999383469l).count(15));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertTrue("size", 0 < statuses.size());



        statuses = twitter1.getUserTimeline(new Paging(1).count(30));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        List<Status> statuses2 = twitter1.getUserTimeline(new Paging(2).count(15));
        assertEquals(statuses2.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses2.get(0))));
        assertEquals(statuses.get(statuses.size() - 1), statuses2.get(statuses2.size() - 1));
    }

    public void testShowStatus() throws Exception {
//        JSONObject json = new JSONObject();
//        json.append("text", " <%}&lt; foobar <&Cynthia>");
//        System.out.println(json.toString());
//        System.out.println(json.getString("text"));
        Status status;
        status = DataObjectFactory.createStatus("{\"text\":\"\\\\u5e30%u5e30 &lt;%}& foobar &lt;&Cynthia&gt;\",\"contributors\":null,\"geo\":null,\"retweeted\":false,\"in_reply_to_screen_name\":null,\"truncated\":false,\"entities\":{\"urls\":[],\"hashtags\":[],\"user_mentions\":[]},\"in_reply_to_status_id_str\":null,\"id\":12029015787307008,\"in_reply_to_user_id_str\":null,\"source\":\"web\",\"favorited\":false,\"in_reply_to_status_id\":null,\"in_reply_to_user_id\":null,\"created_at\":\"Tue Dec 07 06:21:55 +0000 2010\",\"retweet_count\":0,\"id_str\":\"12029015787307008\",\"place\":null,\"user\":{\"location\":\"location:\",\"statuses_count\":13405,\"profile_background_tile\":false,\"lang\":\"en\",\"profile_link_color\":\"0000ff\",\"id\":6358482,\"following\":true,\"favourites_count\":2,\"protected\":false,\"profile_text_color\":\"000000\",\"contributors_enabled\":false,\"description\":\"Hi there, I do test a lot!new\",\"verified\":false,\"profile_sidebar_border_color\":\"87bc44\",\"name\":\"twit4j\",\"profile_background_color\":\"9ae4e8\",\"created_at\":\"Sun May 27 09:52:09 +0000 2007\",\"followers_count\":24,\"geo_enabled\":true,\"profile_background_image_url\":\"http://a3.twimg.com/profile_background_images/179009017/t4j-reverse.gif\",\"follow_request_sent\":false,\"url\":\"http://yusuke.homeip.net/twitter4j/\",\"utc_offset\":-32400,\"time_zone\":\"Alaska\",\"notifications\":false,\"friends_count\":4,\"profile_use_background_image\":true,\"profile_sidebar_fill_color\":\"e0ff92\",\"screen_name\":\"twit4j\",\"id_str\":\"6358482\",\"profile_image_url\":\"http://a3.twimg.com/profile_images/1184543043/t4j-reverse_normal.jpeg\",\"show_all_inline_media\":false,\"listed_count\":3},\"coordinates\":null}");
        assertEquals("\\u5e30%u5e30 <%}& foobar <&Cynthia>", status.getText());

        status = twitter2.showStatus(1000l);
        assertNotNull(DataObjectFactory.getRawJSON(status));
        assertEquals(status, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(status)));
        assertEquals(52, status.getUser().getId());
        Status status2 = twitter1.showStatus(1000l);
        assertEquals(52, status2.getUser().getId());
        assertNotNull(status.getRateLimitStatus());
        assertNotNull(DataObjectFactory.getRawJSON(status2));
        assertEquals(status2, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(status2)));

        status2 = twitter1.showStatus(999383469l);
        assertNotNull(DataObjectFactory.getRawJSON(status2));
        assertEquals(status2, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(status2)));
        assertEquals("01010100 01110010 01101001 01110101 01101101 01110000 01101000       <3", status2.getText());
        status2 = twitter1.showStatus(12029015787307008l);
        assertNotNull(DataObjectFactory.getRawJSON(status2));
        assertEquals(status2, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(status2)));
        System.out.println(DataObjectFactory.getRawJSON(status2));
        assertEquals("\\u5e30%u5e30 <%}& foobar <&Cynthia>", status2.getText());
    }

    public void testStatusMethods() throws Exception {
        String date = new java.util.Date().toString() + "test http://t.co/chEeJss";
        Status status = twitter1.updateStatus(date);
        assertNotNull(DataObjectFactory.getRawJSON(status));
        assertEquals(status, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(status)));
        URLEntity[] entities = status.getURLEntities();
        assertEquals(1, entities.length);
        System.out.println(entities[0]);
        assertEquals("http://t.co/chEeJss", entities[0].getURL().toString());
        assertEquals("http://samuraism.jp/cgi-bin/is01.cgi", entities[0].getExpandedURL().toString());
        assertEquals("samuraism.jp/cgi-bin/is01.c\u2026", entities[0].getDisplayURL());
        assertTrue(0 < entities[0].getStart());
        assertTrue(entities[0].getStart() < entities[0].getEnd());

        assertEquals(date, status.getText());
        Status status2 = twitter2.updateStatus("@" + id1.screenName + " " + date, status.getId());
        assertNotNull(DataObjectFactory.getRawJSON(status2));
        assertEquals(status2, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(status2)));
        assertEquals("@" + id1.screenName + " " + date, status2.getText());
        assertEquals(status.getId(), status2.getInReplyToStatusId());
        assertEquals(twitter1.verifyCredentials().getId(), status2.getInReplyToUserId());
        status = twitter1.destroyStatus(status.getId());
        assertNotNull(DataObjectFactory.getRawJSON(status));
        assertEquals(status, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(status)));
    }

    public void testRetweetMethods() throws Exception {
        List<Status> statuses = twitter1.getRetweetedByMe();
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertIsRetweet(statuses);
        statuses = twitter1.getRetweetedByMe(new Paging(1));
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertIsRetweet(statuses);
        statuses = twitter1.getRetweetedToMe();
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertIsRetweet(statuses);
        statuses = twitter1.getRetweetedToMe(new Paging(1));
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertIsRetweet(statuses);
        statuses = twitter1.getRetweetsOfMe();
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
//        assertIsRetweet(statuses);
        statuses = twitter1.getRetweetsOfMe(new Paging(1));
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
//        assertIsRetweet(statuses);
        statuses = twitter1.getRetweets(18594701629l);
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertIsRetweet(statuses);
        assertTrue(20 < statuses.size());

        for(Status status: statuses){
            if(null != status.getRetweetedStatus()){
                List<User> users = twitter1.getRetweetedBy(status.getRetweetedStatus().getId());
                assertNotNull(DataObjectFactory.getRawJSON(users));
                assertNotNull(DataObjectFactory.getRawJSON(users.get(0)));
                assertNotNull(users);
                assertTrue(users.size() > 1);
                break;
            }
        }
    }

    private void assertIsRetweet(List<Status> statuses) {
        for(Status status : statuses){
            assertTrue(status.getText().startsWith("RT "));
        }
    }

    public void testGeoLocation() throws Exception {
        final double LATITUDE = 12.3456;
        final double LONGITUDE = -34.5678;

        Status withgeo = twitter1.updateStatus(new java.util.Date().toString() + ": updating geo location", new GeoLocation(LATITUDE, LONGITUDE));
        assertNotNull(DataObjectFactory.getRawJSON(withgeo));
        assertEquals(withgeo, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(withgeo)));
        assertTrue(withgeo.getUser().isGeoEnabled());
        assertEquals(LATITUDE, withgeo.getGeoLocation().getLatitude());
        assertEquals(LONGITUDE, withgeo.getGeoLocation().getLongitude());
        assertFalse(twitter2.verifyCredentials().isGeoEnabled());
    }

//    public void testAnnotations() throws Exception {
//    	final String failMessage =
//    		"Annotations were not added to the status, please make sure that your account is whitelisted for Annotations by Twitter";
//    	Annotation annotation = new Annotation("review");
//    	annotation.attribute("content", "Yahoo! landing page").
//    		attribute("url", "http://yahoo.com").attribute("rating", "0.6");
//
//    	StatusUpdate update = new StatusUpdate(new java.util.Date().toString() + ": annotated status");
//    	update.addAnnotation(annotation);
//
//        Status withAnnos = twitterAPI1.updateStatus(update);
//        Annotations annotations = withAnnos.getAnnotations();
//        assertNotNull(failMessage, annotations);
//
//        List<Annotation> annos = annotations.getAnnotations();
//        assertEquals(1, annos.size());
//        assertEquals(annotation, annos.get(0));
//    }

    public void testGetFriendsStatuses() throws Exception {
        PagableResponseList<User> users = twitter1.getFriendsStatuses();
        assertNotNull(DataObjectFactory.getRawJSON(users));
        assertEquals(users.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(users.get(0))));
        assertNotNull("friendsStatuses", users);

        users = twitter1.getFriendsStatuses(numberId);
        assertNotNull(DataObjectFactory.getRawJSON(users));
        assertEquals(users.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(users.get(0))));
        assertNotNull("friendsStatuses", users);
        assertEquals(id1.screenName, users.get(0).getScreenName());

        users = twitter1.getFriendsStatuses(numberIdId);
        assertNotNull(DataObjectFactory.getRawJSON(users));
        assertEquals(users.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(users.get(0))));
        assertNotNull("friendsStatuses", users);
        assertEquals(id1.screenName, users.get(0).getScreenName());

        try {
            users = unauthenticated.getFriendsStatuses("yusukey");
            assertNotNull(DataObjectFactory.getRawJSON(users));
            assertNotNull("friendsStatuses", users);
            assertEquals(users.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(users.get(0))));
        } catch (TwitterException te) {
            // is being rate limited
            assertEquals(400, te.getStatusCode());
        }
    }

    public void testRelationship() throws Exception {
        //  TESTING PRECONDITIONS:
        //  1) id1 is followed by "followsOneWay", but not following "followsOneWay"
        Relationship rel1 = twitter1.showFriendship(id1.screenName, followsOneWay);
        assertNotNull(DataObjectFactory.getRawJSON(rel1));
        assertEquals(rel1, DataObjectFactory.createRelationship(DataObjectFactory.getRawJSON(rel1)));

        // test second precondition
        assertNotNull(rel1);
        assertTrue(rel1.isSourceFollowedByTarget());
        assertFalse(rel1.isSourceFollowingTarget());
        assertTrue(rel1.isTargetFollowingSource());
        assertFalse(rel1.isTargetFollowedBySource());

        //  2) best_friend1 is following and followed by best_friend2
        Relationship rel2 = twitter1.showFriendship(bestFriend1.screenName, bestFriend2.screenName);
        assertNull(DataObjectFactory.getRawJSON(rel1));
        assertNotNull(DataObjectFactory.getRawJSON(rel2));
        assertEquals(rel2, DataObjectFactory.createRelationship(DataObjectFactory.getRawJSON(rel2)));

        // test second precondition
        assertNotNull(rel2);
        assertTrue(rel2.isSourceFollowedByTarget());
        assertTrue(rel2.isSourceFollowingTarget());
        assertTrue(rel2.isTargetFollowingSource());
        assertTrue(rel2.isTargetFollowedBySource());

        // test equality
        Relationship rel3 = twitter1.showFriendship(id1.screenName, followsOneWay);
        assertNotNull(DataObjectFactory.getRawJSON(rel3));
        assertEquals(rel3, DataObjectFactory.createRelationship(DataObjectFactory.getRawJSON(rel3)));
        assertEquals(rel1, rel3);
        assertFalse(rel1.equals(rel2));
    }

    private void assertIDExsits(String assertion, IDs ids, int idToFind) {
        boolean found = false;
        for (int id : ids.getIDs()) {
            if (id == idToFind) {
                found = true;
                break;
            }
        }
        assertTrue(assertion, found);
    }


    public void testFriendships() throws Exception {
        IDs ids;
        ids = twitter3.getIncomingFriendships(-1);
        assertNotNull(DataObjectFactory.getRawJSON(ids));
        assertEquals(ids, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(ids)));
        assertTrue(ids.getIDs().length > 0);
        ids = twitter2.getOutgoingFriendships(-1);
        assertNotNull(DataObjectFactory.getRawJSON(ids));
        assertEquals(ids, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(ids)));
        assertTrue(ids.getIDs().length > 0);
    }

    public void testSocialGraphMethods() throws Exception {
        IDs ids;
        ids = twitter1.getFriendsIDs();
        assertNotNull(DataObjectFactory.getRawJSON(ids));
        int yusukey = 4933401;
        assertIDExsits("twit4j is following yusukey", ids, yusukey);
        int ryunosukey = 48528137;
        ids = twitter1.getFriendsIDs(ryunosukey);
        assertNotNull(DataObjectFactory.getRawJSON(ids));
        assertEquals(ids, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(ids)));
        assertEquals("ryunosukey is not following anyone", 0, ids.getIDs().length);
        ids = twitter1.getFriendsIDs("yusukey");
        assertNotNull(DataObjectFactory.getRawJSON(ids));
        assertIDExsits("yusukey is following ryunosukey", ids, ryunosukey);
        IDs obamaFollowers;
        obamaFollowers = twitter1.getFollowersIDs("barackobama");
        assertNotNull(DataObjectFactory.getRawJSON(obamaFollowers));
        assertEquals(obamaFollowers, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(obamaFollowers)));
        assertTrue(obamaFollowers.hasNext());
        assertFalse(obamaFollowers.hasPrevious());
        obamaFollowers = twitter1.getFollowersIDs("barackobama", obamaFollowers.getNextCursor());
        assertEquals(obamaFollowers, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(obamaFollowers)));
        assertNotNull(DataObjectFactory.getRawJSON(obamaFollowers));
        assertTrue(obamaFollowers.hasNext());
        assertTrue(obamaFollowers.hasPrevious());

        obamaFollowers = twitter1.getFollowersIDs(813286);
        assertNotNull(DataObjectFactory.getRawJSON(obamaFollowers));
        assertEquals(obamaFollowers, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(obamaFollowers)));
        assertTrue(obamaFollowers.hasNext());
        assertFalse(obamaFollowers.hasPrevious());
        obamaFollowers = twitter1.getFollowersIDs(813286, obamaFollowers.getNextCursor());
        assertNotNull(DataObjectFactory.getRawJSON(obamaFollowers));
        assertEquals(obamaFollowers, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(obamaFollowers)));
        assertTrue(obamaFollowers.hasNext());
        assertTrue(obamaFollowers.hasPrevious());

        IDs obamaFriends;
        obamaFriends = twitter1.getFriendsIDs("barackobama");
        assertNull(DataObjectFactory.getRawJSON(obamaFollowers));
        assertNotNull(DataObjectFactory.getRawJSON(obamaFriends));
        assertEquals(obamaFriends, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(obamaFriends)));
        assertTrue(obamaFriends.hasNext());
        assertFalse(obamaFriends.hasPrevious());
        obamaFriends = twitter1.getFriendsIDs("barackobama", obamaFriends.getNextCursor());
        assertNull(DataObjectFactory.getRawJSON(obamaFollowers));
        assertNotNull(DataObjectFactory.getRawJSON(obamaFriends));
        assertEquals(obamaFriends, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(obamaFriends)));
        assertTrue(obamaFriends.hasNext());
        assertTrue(obamaFriends.hasPrevious());

        obamaFriends = twitter1.getFriendsIDs(813286);
        assertNull(DataObjectFactory.getRawJSON(obamaFollowers));
        assertNotNull(DataObjectFactory.getRawJSON(obamaFriends));
        assertEquals(obamaFriends, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(obamaFriends)));
        assertTrue(obamaFriends.hasNext());
        assertFalse(obamaFriends.hasPrevious());
        obamaFriends = twitter1.getFriendsIDs(813286, obamaFriends.getNextCursor());
        assertNull(DataObjectFactory.getRawJSON(obamaFollowers));
        assertNotNull(DataObjectFactory.getRawJSON(obamaFriends));
        assertEquals(obamaFriends, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(obamaFriends)));
        assertTrue(obamaFriends.hasNext());
        assertTrue(obamaFriends.hasPrevious());

        try {
            twitter2.createFriendship(id1.screenName);
        } catch (TwitterException ignore) {
        }
        ids = twitter1.getFollowersIDs();
        assertNotNull(DataObjectFactory.getRawJSON(ids));
        assertEquals(ids, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(ids)));
        assertIDExsits("twit4j2 is following twit4j", ids, 6377362);
        ids = twitter1.getFollowersIDs(ryunosukey);
        assertNotNull(DataObjectFactory.getRawJSON(ids));
        assertEquals(ids, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(ids)));
        assertIDExsits("yusukey is following ryunosukey", ids, yusukey);
        ids = twitter1.getFollowersIDs("ryunosukey");
        assertNotNull(DataObjectFactory.getRawJSON(ids));
        assertEquals(ids, DataObjectFactory.createIDs(DataObjectFactory.getRawJSON(ids)));
        assertIDExsits("yusukey is following ryunosukey", ids, yusukey);
    }

    public void testAccountMethods() throws Exception {
        User original = twitter1.verifyCredentials();
        assertNotNull(DataObjectFactory.getRawJSON(original));
        assertEquals(original, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(original)));
        if (original.getScreenName().endsWith("new") ||
                original.getName().endsWith("new")) {
            original = twitter1.updateProfile(
                    "twit4j", "http://yusuke.homeip.net/twitter4j/"
                    , "location:", "Hi there, I do test a lot!new");

        }
        String newName, newURL, newLocation, newDescription;
        String neu = "new";
        newName = original.getName() + neu;
        newURL = original.getURL() + neu;
        newLocation = new Date().toString();
        newDescription = original.getDescription() + neu;

        User altered = twitter1.updateProfile(
                newName, newURL, newLocation, newDescription);
        assertNotNull(DataObjectFactory.getRawJSON(altered));
        assertEquals(original, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(original)));
        assertEquals(altered, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(altered)));
        twitter1.updateProfile(original.getName(), original.getURL().toString(), original.getLocation(), original.getDescription());
        assertEquals(newName, altered.getName());
        assertEquals(newURL, altered.getURL().toString());
        assertEquals(newLocation, altered.getLocation());
        assertEquals(newDescription, altered.getDescription());

        try {
            new TwitterFactory().getInstance("doesnotexist--", "foobar").verifyCredentials();
            fail("should throw TwitterException");
        } catch (TwitterException te) {
        }

        assertTrue(twitterAPIBestFriend1.existsFriendship(bestFriend1.screenName, bestFriend2.screenName));
        assertFalse(twitter1.existsFriendship(id1.screenName, "al3x"));

        User eu;
        eu = twitter1.updateProfileColors("f00", "f0f", "0ff", "0f0", "f0f");
        assertNotNull(DataObjectFactory.getRawJSON(eu));
        assertEquals(eu, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(eu)));
        assertEquals("f00", eu.getProfileBackgroundColor());
        assertEquals("f0f", eu.getProfileTextColor());
        assertEquals("0ff", eu.getProfileLinkColor());
        assertEquals("0f0", eu.getProfileSidebarFillColor());
        assertEquals("f0f", eu.getProfileSidebarBorderColor());
        assertTrue(eu.isProfileUseBackgroundImage());
        assertFalse(eu.isShowAllInlineMedia());
        assertTrue(0 <= eu.getListedCount());
        eu = twitter1.updateProfileColors("f0f", "f00", "f0f", "0ff", "0f0");
        assertNotNull(DataObjectFactory.getRawJSON(eu));
        assertEquals(eu, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(eu)));
        assertEquals("f0f", eu.getProfileBackgroundColor());
        assertEquals("f00", eu.getProfileTextColor());
        assertEquals("f0f", eu.getProfileLinkColor());
        assertEquals("0ff", eu.getProfileSidebarFillColor());
        assertEquals("0f0", eu.getProfileSidebarBorderColor());
        eu = twitter1.updateProfileColors("87bc44", "9ae4e8", "000000", "0000ff", "e0ff92");
        assertNotNull(DataObjectFactory.getRawJSON(eu));
        assertEquals(eu, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(eu)));
        assertEquals("87bc44", eu.getProfileBackgroundColor());
        assertEquals("9ae4e8", eu.getProfileTextColor());
        assertEquals("000000", eu.getProfileLinkColor());
        assertEquals("0000ff", eu.getProfileSidebarFillColor());
        assertEquals("e0ff92", eu.getProfileSidebarBorderColor());
        eu = twitter1.updateProfileColors("f0f", null, "f0f", null, "0f0");
        assertNotNull(DataObjectFactory.getRawJSON(eu));
        assertEquals(eu, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(eu)));
        assertEquals("f0f", eu.getProfileBackgroundColor());
        assertEquals("9ae4e8", eu.getProfileTextColor());
        assertEquals("f0f", eu.getProfileLinkColor());
        assertEquals("0000ff", eu.getProfileSidebarFillColor());
        assertEquals("0f0", eu.getProfileSidebarBorderColor());
        eu = twitter1.updateProfileColors(null, "f00", null, "0ff", null);
        assertNotNull(DataObjectFactory.getRawJSON(eu));
        assertEquals(eu, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(eu)));
        assertEquals("f0f", eu.getProfileBackgroundColor());
        assertEquals("f00", eu.getProfileTextColor());
        assertEquals("f0f", eu.getProfileLinkColor());
        assertEquals("0ff", eu.getProfileSidebarFillColor());
        assertEquals("0f0", eu.getProfileSidebarBorderColor());
        eu = twitter1.updateProfileColors("9ae4e8", "000000", "0000ff", "e0ff92", "87bc44");
        assertNotNull(DataObjectFactory.getRawJSON(eu));
        assertEquals(eu, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(eu)));
        assertEquals("9ae4e8", eu.getProfileBackgroundColor());
        assertEquals("000000", eu.getProfileTextColor());
        assertEquals("0000ff", eu.getProfileLinkColor());
        assertEquals("e0ff92", eu.getProfileSidebarFillColor());
        assertEquals("87bc44", eu.getProfileSidebarBorderColor());
    }

    public void testAccountProfileImageUpdates() throws Exception {
        User user = twitter1.updateProfileImage(getRandomlyChosenFile());
        assertNotNull(DataObjectFactory.getRawJSON(user));
        // tile randomly
        User user2 = twitter1.updateProfileBackgroundImage(getRandomlyChosenFile(),
                (5 < System.currentTimeMillis() % 5));
        assertNotNull(DataObjectFactory.getRawJSON(user2));
        assertEquals(user2, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(user2)));
    }
    static final String[] files = {"src/test/resources/t4j-reverse.jpeg",
            "src/test/resources/t4j-reverse.png",
            "src/test/resources/t4j-reverse.gif",
            "src/test/resources/t4j.jpeg",
            "src/test/resources/t4j.png",
            "src/test/resources/t4j.gif",
    };

    public static File getRandomlyChosenFile(){
        int rand = (int) (System.currentTimeMillis() % 6);
        File file = new File(files[rand]);
        if(!file.exists()){
            file = new File("twitter4j-core/"+ files[rand]);
        }
        return file;
    }

    public void testFavoriteMethods() throws Exception {
        Status status = twitter1.getHomeTimeline(new Paging().count(1)).get(0);
        assertNotNull(DataObjectFactory.getRawJSON(status));
        assertEquals(status, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(status)));
        status = twitter2.createFavorite(status.getId());
        assertNotNull(DataObjectFactory.getRawJSON(status));
        assertEquals(status, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(status)));
        assertTrue(twitter2.getFavorites().size() > 0);
        try {
            twitter2.destroyFavorite(status.getId());
        } catch (TwitterException te) {
            // sometimes destorying favorite fails with 404
            assertEquals(404, te.getStatusCode());
        }
    }

    public void testFollowers() throws Exception {
        PagableResponseList<User> actualReturn = twitter1.getFollowersStatuses();
        assertNotNull(DataObjectFactory.getRawJSON(actualReturn));
        assertEquals(actualReturn.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(actualReturn.get(0))));
        assertTrue(actualReturn.size() > 0);
        actualReturn = twitter1.getFollowersStatuses();
        assertNotNull(DataObjectFactory.getRawJSON(actualReturn));
        assertEquals(actualReturn.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(actualReturn.get(0))));
        assertTrue(actualReturn.size() > 0);
        assertFalse(actualReturn.hasNext());
        assertFalse(actualReturn.hasPrevious());
        actualReturn = twitter1.getFollowersStatuses(actualReturn.getNextCursor());
        assertNotNull(DataObjectFactory.getRawJSON(actualReturn));
        assertEquals(0, actualReturn.size());

        actualReturn = twitter2.getFollowersStatuses();
        assertNotNull(DataObjectFactory.getRawJSON(actualReturn));
        assertEquals(actualReturn.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(actualReturn.get(0))));
        assertTrue(actualReturn.size() > 0);
        actualReturn = twitter2.getFollowersStatuses("yusukey");
        assertNotNull(DataObjectFactory.getRawJSON(actualReturn));
        assertEquals(actualReturn.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(actualReturn.get(0))));
        assertTrue(actualReturn.size() > 60);
        actualReturn = twitter2.getFollowersStatuses("yusukey", actualReturn.getNextCursor());
        assertNotNull(DataObjectFactory.getRawJSON(actualReturn));
        assertEquals(actualReturn.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(actualReturn.get(0))));
        assertTrue(actualReturn.size() > 10);
        // - Issue 1572: Lists previous cursor returns empty results
        // http://code.google.com/p/twitter-api/issues/detail?id=1572
//        actualReturn = twitterAPI2.getFollowersStatuses("yusukey", actualReturn.getPreviousCursor());
//        assertTrue(actualReturn.size() > 10);
    }

    public void testDirectMessages() throws Exception {
        String expectedReturn = new Date() + ":directmessage test";
        DirectMessage actualReturn = twitter1.sendDirectMessage("twit4jnoupdate", expectedReturn);
        assertNotNull(DataObjectFactory.getRawJSON(actualReturn));
        assertEquals(actualReturn, DataObjectFactory.createDirectMessage(DataObjectFactory.getRawJSON(actualReturn)));
        assertEquals(expectedReturn, actualReturn.getText());
        assertEquals(id1.screenName, actualReturn.getSender().getScreenName());
        assertEquals("twit4jnoupdate", actualReturn.getRecipient().getScreenName());
        List<DirectMessage> actualReturnList = twitter1.getDirectMessages();
        assertNull(DataObjectFactory.getRawJSON(actualReturn));
        assertNotNull(DataObjectFactory.getRawJSON(actualReturnList));
        assertEquals(actualReturnList.get(0), DataObjectFactory.createDirectMessage(DataObjectFactory.getRawJSON(actualReturnList.get(0))));
        assertTrue(1 <= actualReturnList.size());
    }

    public void testCreateDestroyFriend() throws Exception {
        User user;
        try {
            user = twitter2.destroyFriendship(id1.screenName);
            assertNotNull(DataObjectFactory.getRawJSON(user));
            assertEquals(user, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(user)));
        } catch (TwitterException te) {
            //ensure destory id1 before the actual test
            //ensure destory id1 before the actual test
        }

        try {
            user = twitter2.destroyFriendship(id1.screenName);
            assertNotNull(DataObjectFactory.getRawJSON(user));
            assertEquals(user, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(user)));
        } catch (TwitterException te) {
            assertEquals(403, te.getStatusCode());
        }
        user = twitter2.createFriendship(id1.screenName, true);
        assertNotNull(DataObjectFactory.getRawJSON(user));
        assertEquals(user, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(user)));
        assertEquals(id1.screenName, user.getScreenName());
        // the Twitter API is not returning appropriate notifications value
        // http://code.google.com/p/twitter-api/issues/detail?id=474
//        User detail = twitterAPI2.showUser(id1);
//        assertTrue(detail.isNotificationEnabled());
        try {
            user = twitter2.createFriendship(id2.screenName);
            fail("shouldn't be able to befrinend yourself");
        } catch (TwitterException te) {
            assertEquals(403, te.getStatusCode());
        }
        try {
            user = twitter2.createFriendship("doesnotexist--");
            fail("non-existing user");
        } catch (TwitterException te) {
            //now befriending with non-existing user returns 404
            //http://groups.google.com/group/twitter-development-talk/browse_thread/thread/bd2a912b181bc39f
            assertEquals(404, te.getStatusCode());
        }

    }

    public void testGetMentions() throws Exception {
        Status status = twitter2.updateStatus("@" + id1.screenName + " reply to id1 " + new java.util.Date());
        assertNotNull(DataObjectFactory.getRawJSON(status));
        List<Status> statuses = twitter1.getMentions();
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertTrue(statuses.size() > 0);

        statuses = twitter1.getMentions(new Paging(1));
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertTrue(statuses.size() > 0);
        statuses = twitter1.getMentions(new Paging(1));
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertTrue(statuses.size() > 0);
        statuses = twitter1.getMentions(new Paging(1, 1l));
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertTrue(statuses.size() > 0);
        statuses = twitter1.getMentions(new Paging(1l));
        assertNotNull(DataObjectFactory.getRawJSON(statuses));
        assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        assertTrue(statuses.size() > 0);
    }

    public void testNotification() throws Exception {
        try {
            twitter1.disableNotification("twit4jprotected");
        } catch (TwitterException te) {
        }
        User user1 = twitter1.enableNotification("twit4jprotected");
        assertNotNull(DataObjectFactory.getRawJSON(user1));
        assertEquals(user1, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(user1)));
        User user2 = twitter1.disableNotification("twit4jprotected");
        assertNotNull(DataObjectFactory.getRawJSON(user2));
        assertEquals(user2, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(user2)));
    }

    public void testBlock() throws Exception {
        User user1 = twitter2.createBlock(id1.screenName);
        assertNotNull(DataObjectFactory.getRawJSON(user1));
        assertEquals(user1, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(user1)));
        User user2 = twitter2.destroyBlock(id1.screenName);
        assertNotNull(DataObjectFactory.getRawJSON(user2));
        assertEquals(user2, DataObjectFactory.createUser(DataObjectFactory.getRawJSON(user2)));
        assertFalse(twitter1.existsBlock("twit4j2"));
        assertTrue(twitter1.existsBlock("twit4jblock"));
        List<User> users = twitter1.getBlockingUsers();
        assertNotNull(DataObjectFactory.getRawJSON(users));
        assertEquals(users.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(users.get(0))));
        assertEquals(1, users.size());
        assertEquals(39771963, users.get(0).getId());
        users = twitter1.getBlockingUsers(1);
        assertNotNull(DataObjectFactory.getRawJSON(users));
        assertEquals(users.get(0), DataObjectFactory.createUser(DataObjectFactory.getRawJSON(users.get(0))));
        assertEquals(1, users.size());
        assertEquals(39771963, users.get(0).getId());

        IDs ids = twitter1.getBlockingUsersIDs();
        assertNull(DataObjectFactory.getRawJSON(users));
        assertNotNull(DataObjectFactory.getRawJSON(ids));
        assertEquals(1, ids.getIDs().length);
        assertEquals(39771963, ids.getIDs()[0]);
    }

    public void testLocalTrendsMethods() throws Exception {
        ResponseList<Location> locations;
        locations = twitter1.getAvailableTrends();
        assertNotNull(DataObjectFactory.getRawJSON(locations));
        assertEquals(locations.get(0), DataObjectFactory.createLocation(DataObjectFactory.getRawJSON(locations.get(0))));
        assertTrue(locations.size() > 0);
        locations = twitter1.getAvailableTrends(new GeoLocation(0,0));
        assertNotNull(DataObjectFactory.getRawJSON(locations));
        assertTrue(locations.size() > 0);

        Trends trends = twitter1.getLocationTrends(locations.get(0).getWoeid());
        System.out.println(DataObjectFactory.getRawJSON(trends));
        assertEquals(trends, DataObjectFactory.createTrends(DataObjectFactory.getRawJSON(trends)));
        assertNull(DataObjectFactory.getRawJSON(locations));
        System.out.println(DataObjectFactory.getRawJSON(trends));
        assertNotNull(DataObjectFactory.getRawJSON(trends));
        assertEquals(locations.get(0), trends.getLocation());
        assertTrue(trends.getTrends().length > 0);

        try {
            trends = twitter1.getLocationTrends(2345889/*woeid of Tokyo*/);
            fail("should fail.");
        } catch (Exception ignore) {
        }
        assertEquals(locations.get(0), trends.getLocation());
        assertTrue(trends.getTrends().length > 0);
    }

    RateLimitStatus rateLimitStatus = null;
    boolean accountLimitStatusAcquired;
    boolean ipLimitStatusAcquired;
    //need to think of a way to test this, perhaps mocking out Twitter is the way to go
    public void testRateLimitStatus() throws Exception {
        RateLimitStatus rateLimitStatus = twitter1.getRateLimitStatus();
        assertNotNull(DataObjectFactory.getRawJSON(rateLimitStatus));
        assertEquals(rateLimitStatus, DataObjectFactory.createRateLimitStatus(DataObjectFactory.getRawJSON(rateLimitStatus)));
        assertTrue(10 < rateLimitStatus.getHourlyLimit());
        assertTrue(10 < rateLimitStatus.getRemainingHits());

        twitter1.setRateLimitStatusListener(new RateLimitStatusListener() {
            public void onRateLimitStatus(RateLimitStatusEvent event) {
                System.out.println("onRateLimitStatus"+event);
                accountLimitStatusAcquired = event.isAccountRateLimitStatus();
                ipLimitStatusAcquired = event.isIPRateLimitStatus();
                TwitterTest.this.rateLimitStatus = event.getRateLimitStatus();
            }

            public void onRateLimitReached(RateLimitStatusEvent event) {

            }

        });
        // the listener doesn't implement serializable and deserialized form should not be equal to the original object
        assertDeserializedFormIsNotEqual(twitter1);

        unauthenticated.setRateLimitStatusListener(new RateLimitStatusListener() {
            public void onRateLimitStatus(RateLimitStatusEvent event) {
                accountLimitStatusAcquired = event.isAccountRateLimitStatus();
                ipLimitStatusAcquired = event.isIPRateLimitStatus();
                TwitterTest.this.rateLimitStatus = event.getRateLimitStatus();
            }
            public void onRateLimitReached(RateLimitStatusEvent event){
            }
        });
        // the listener doesn't implement serializable and deserialized form should not be equal to the original object
        assertDeserializedFormIsNotEqual(unauthenticated);

        twitter1.getMentions();
        assertTrue(accountLimitStatusAcquired);
        assertFalse(ipLimitStatusAcquired);
        RateLimitStatus previous = this.rateLimitStatus;
        twitter1.getMentions();
        assertTrue(accountLimitStatusAcquired);
        assertFalse(ipLimitStatusAcquired);
        assertTrue(previous.getRemainingHits() > this.rateLimitStatus.getRemainingHits());
        assertEquals(previous.getHourlyLimit(), this.rateLimitStatus.getHourlyLimit());

        try {
            unauthenticated.getPublicTimeline();
            assertFalse(accountLimitStatusAcquired);
            assertTrue(ipLimitStatusAcquired);
            previous = this.rateLimitStatus;
            unauthenticated.getPublicTimeline();
            assertFalse(accountLimitStatusAcquired);
            assertTrue(ipLimitStatusAcquired);
            assertTrue(previous.getRemainingHits() > this.rateLimitStatus.getRemainingHits());
            assertEquals(previous.getHourlyLimit(), this.rateLimitStatus.getHourlyLimit());
        } catch (TwitterException te) {
            // is being rate limited;
            assertEquals(400, te.getStatusCode());
        }
    }

    /* Spam Reporting Methods */
    public void testReportSpammerSavedSearches() throws Exception {
        // Not sure they're accepting multiple spam reports for the same user.
        // Do we really need to test this method? How?
    }

    /* Saved Searches Methods */
    public void testSavedSearches() throws Exception {
        List<SavedSearch> list = twitter1.getSavedSearches();
        assertNotNull(DataObjectFactory.getRawJSON(list));
        for (SavedSearch savedSearch : list) {
            twitter1.destroySavedSearch(savedSearch.getId());
        }
        SavedSearch ss1 = twitter1.createSavedSearch("my search");
        assertNotNull(DataObjectFactory.getRawJSON(ss1));
        assertEquals(ss1, DataObjectFactory.createSavedSearch(DataObjectFactory.getRawJSON(ss1)));
        assertEquals("my search", ss1.getQuery());
        assertEquals(-1, ss1.getPosition());
        list = twitter1.getSavedSearches();
        assertNotNull(DataObjectFactory.getRawJSON(list));
        assertEquals(list.get(0), DataObjectFactory.createSavedSearch(DataObjectFactory.getRawJSON(list.get(0))));
        // http://code.google.com/p/twitter-api/issues/detail?id=1032
        // the saved search may not be immediately available
        assertTrue(0 <= list.size());
        try {
            SavedSearch ss2 = twitter1.destroySavedSearch(ss1.getId());
            assertEquals(ss1, ss2);
        } catch (TwitterException te) {
            // sometimes it returns 404 or 500 when its out of sync.
            assertTrue(404 == te.getStatusCode() || 500 == te.getStatusCode());
        }
    }

    public void testGeoMethods() throws Exception {
        GeoQuery query;
        ResponseList<Place> places;
        query = new GeoQuery(new GeoLocation(0, 0));

        places = twitter1.reverseGeoCode(query);
        assertEquals(0, places.size());

        query = new GeoQuery(new GeoLocation(37.78215, -122.40060));
        places = twitter1.reverseGeoCode(query);
        assertNotNull(DataObjectFactory.getRawJSON(places));
        assertEquals(places.get(0), DataObjectFactory.createPlace(DataObjectFactory.getRawJSON(places.get(0))));

        assertTrue(places.size() > 0);
        places = twitter1.getNearbyPlaces(query);
        assertNotNull(DataObjectFactory.getRawJSON(places));
        for(Place place : places){
            System.out.println(place.getName());
        }
        assertTrue(places.size() > 0);
        places = twitter1.searchPlaces(query);
        assertNotNull(DataObjectFactory.getRawJSON(places));
        assertEquals(places.get(0), DataObjectFactory.createPlace(DataObjectFactory.getRawJSON(places.get(0))));
        assertTrue(places.size() > 0);
        places = twitter1.getSimilarPlaces(new GeoLocation(37.78215, -122.40060), "SoMa", null, null);
        assertNotNull(DataObjectFactory.getRawJSON(places));
        assertEquals(places.get(0), DataObjectFactory.createPlace(DataObjectFactory.getRawJSON(places.get(0))));
        assertTrue(places.size() > 0);

        try{
            Place place = this.unauthenticated.getGeoDetails("5a110d312052166f");
            assertNotNull(DataObjectFactory.getRawJSON(place));
            assertEquals(place, DataObjectFactory.createPlace(DataObjectFactory.getRawJSON(place)));
            assertEquals("San Francisco, CA", place.getFullName());
            assertEquals("California, US", place.getContainedWithIn()[0].getFullName());
        } catch (TwitterException te) {
            // is being rate limited
            assertEquals(400, te.getStatusCode());
        }
        String sanFrancisco = "5a110d312052166f";
        Status status = twitter1.updateStatus(new StatusUpdate(new java.util.Date() + " status with place").
                placeId(sanFrancisco));
        assertNotNull(DataObjectFactory.getRawJSON(status));
        assertEquals(status, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(status)));
        assertEquals(sanFrancisco, status.getPlace().getId());
        assertEquals(null, status.getContributors());
    }
    public void testLegalResources() throws Exception{
        assertNotNull(twitter1.getTermsOfService());
        assertNotNull(twitter1.getPrivacyPolicy());
    }

    public void testRelatedResults() throws Exception {
        RelatedResults relatedResults = twitter1.getRelatedResults(999383469l);
        assertNotNull(relatedResults);
        ResponseList<Status> statuses;
        statuses = relatedResults.getTweetsFromUser();
        if(statuses.size() > 0){
            assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        }
        statuses = relatedResults.getTweetsWithConversation();
        if(statuses.size() > 0){
            assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        }
        statuses = relatedResults.getTweetsWithReply();
        if(statuses.size() > 0){
            assertEquals(statuses.get(0), DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(statuses.get(0))));
        }
    }

    public void testTest() throws Exception {
        assertTrue(twitter2.test());
    }

    public void testEntities() throws Exception {
        Status status = twitter2.showStatus(22035985122L);
        assertNotNull(DataObjectFactory.getRawJSON(status));
        assertEquals(status, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(status)));
        assertEquals(2, status.getUserMentions().length);
        assertEquals(1, status.getURLs().length);

        User user1 = status.getUserMentions()[0];
        assertEquals(20263710, user1.getId());
        assertEquals("rabois", user1.getScreenName());
        assertEquals("Keith Rabois", user1.getName());
        assertEquals(new URL("http://j.mp/cHv0VS"), status.getURLs()[0]);

        status = twitter2.showStatus(22043496385L);
        assertNotNull(DataObjectFactory.getRawJSON(status));
        assertEquals(status, DataObjectFactory.createStatus(DataObjectFactory.getRawJSON(status)));
        assertEquals(2, status.getHashtags().length);
        assertEquals("pilaf", status.getHashtags()[0]);
        assertEquals("recipe", status.getHashtags()[1]);
    }
}
