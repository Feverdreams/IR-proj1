/*
 * <author>Han He</author>
 * <email>me@hankcs.com</email>
 * <create-date>2020-02-23 7:40 PM</create-date>
 *
 * <copyright file="Topic.java">
 * Copyright (c) 2020, Han He. All Rights Reserved, http://www.hankcs.com/
 * See LICENSE file in the project root for full license information.
 * </copyright>
 */

/**
 * @author hankcs
 */
public class Topic
{
    String title;
    String desc;

    String getQuery()
    {
        String query = title + "\n" + desc;
        query = query.replace("/", " ")
                .replace("(", " ").replace(")", " ")
                .replace("?", " ").replace('"', ' ');
        return query;
    }
}
