
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

        <#list posts as post>
        post : ${post} <br>
        <#else>
        nothing
        </#list>

        </div>
    </body>
</html>
