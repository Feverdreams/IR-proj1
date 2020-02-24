/*
 * <author>Han He</author>
 * <email>me@hankcs.com</email>
 * <create-date>2020-02-23 7:11 PM</create-date>
 *
 * <copyright file="Utils.java">
 * Copyright (c) 2020, Han He. All Rights Reserved, http://www.hankcs.com/
 * See LICENSE file in the project root for full license information.
 * </copyright>
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author hankcs
 */
public class Utils
{
    public static List<Doc> readDoc(BufferedReader reader) throws IOException
    {
        String line;
        StringBuilder sb = new StringBuilder();
        List<Doc> docs = new LinkedList<>();
        Doc doc = null;
        while ((line = reader.readLine()) != null)
        {
            if (line.equals("<DOC>"))
            {
                doc = new Doc();
            }
            else if (line.equals("</DOC>"))
            {
                doc.content = sb.toString();
                docs.add(doc);
                sb.setLength(0);
            }
            else if (!line.startsWith("<"))
            {
                sb.append(line);
            }
            else if (line.startsWith("<DOCNO>"))
            {
                String[] cells = line.split("\\s");
                doc.docno =  cells[1];
            }
        }
        return docs;
    }

}
