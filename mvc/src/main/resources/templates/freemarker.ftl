<!DOCTYPE html>
<html>
    <head>
        <title>Simple Blog Posts</title>
        <meta charset="UTF-8"/>
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    </head>
    <body>
        <h1>All posts</h1>
        <div>
            <table>
                <thead>
                    <tr>
                        <th> ID</th>
                        <th>Title </th>
                        <th>Content</th>
                    </tr>
                </thead>
                <tbody>

                        <#list posts as post>        
                        <tr>
                            <td>${post.id}</td>
                            <td>${post.title}</td>
                            <td>${post.content}</td>
                        </tr>
                        <#else>
                        nothing
                        </#list>

                </tbody>
            </table>  

        </div>
    </body>
</html>
