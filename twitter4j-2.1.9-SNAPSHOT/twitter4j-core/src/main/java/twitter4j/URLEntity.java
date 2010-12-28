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

import java.net.URL;

/**
 * A data interface representing one single of a URL entity.
 *
 * @author Mocel - mocel at guma.jp
 * @since Twitter4J 2.1.9
 */
public interface URLEntity extends java.io.Serializable {

    /**
     * Returns the URL mentioned in the tweet.
     *
     * @return the mentioned URL
     */
    URL getURL();

    /**
     * Returns the expanded URL if mentioned URL is shorten.
     *
     * @return the expanded URL if mentioned URL is shorten, or null if no shorten URL was mentioned.
     */
    URL getExpandedURL();

    /**
     * Returns the display URL if mentioned URL is shorten.
     *
     * @return the display URL if mentioned URL is shorten, or null if no shorten URL was mentioned.
     */
    String getDisplayURL();

    /**
     * Returns the index of the start character of the URL mentioned in the tweet.
     *
     * @return the index of the start character of the URL mentioned in the tweet, or -1 if no URL was mentioned.
     */
    int getStart();

    /**
     * Returns the index of the end character of the URL mentioned in the tweet.
     *
     * @return the index of the end character of the URL mentioned in the tweet, or -1 if no URL was mentioned.
     */
    int getEnd();
}
