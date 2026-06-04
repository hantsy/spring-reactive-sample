$root = 'D:\hantsylabs\spring-reactive-sample'
Set-Location -Path $root
$modifiedFiles = @()
$files = Get-ChildItem -Path $root -Filter pom.xml -Recurse | Where-Object { $_.FullName -notmatch '\\legacy\\' }
Write-Host "Found $($files.Count) pom.xml files to check"
foreach ($f in $files) {
    $file = $f.FullName
    Write-Host "\n--- Processing $file ---"
    try { [xml]$xml = Get-Content -Raw -Path $file } catch { Write-Warning ('Failed to load ' + $file + ': ' + $_); continue }

    $hasNs = $xml.DocumentElement.NamespaceURI -ne $null -and $xml.DocumentElement.NamespaceURI -ne ''
    if ($hasNs) {
        $nsUri = $xml.DocumentElement.NamespaceURI
        $nsManager = New-Object System.Xml.XmlNamespaceManager($xml.NameTable)
        $nsManager.AddNamespace('m',$nsUri)
    }
    function selNodes([string]$xp) {
        if ($hasNs) { return $xml.SelectNodes($xp, $nsManager) } else { return $xml.SelectNodes($xp) }
    }
    function selNode([string]$xp) {
        if ($hasNs) { return $xml.SelectSingleNode($xp, $nsManager) } else { return $xml.SelectSingleNode($xp) }
    }
    function createElem([string]$name) {
        if ($hasNs) { return $xml.CreateElement($name, $nsUri) } else { return $xml.CreateElement($name) }
    }

    $madeChange = $false

    # find lombok dependency
    if ($hasNs) {
        $depNodes = selNodes("//m:dependency[m:groupId='org.projectlombok' and m:artifactId='lombok']")
    } else {
        $depNodes = selNodes("//dependency[groupId='org.projectlombok' and artifactId='lombok']")
    }

    $versionText = $null
    if ($depNodes -and $depNodes.Count -gt 0) {
        foreach ($dep in $depNodes) {
            if ($hasNs) { $opt = $dep.SelectSingleNode('m:optional', $nsManager) } else { $opt = $dep.SelectSingleNode('optional') }
            if ($opt -eq $null) {
                $optElem = createElem('optional')
                $optElem.InnerText = 'true'
                $dep.AppendChild($optElem) | Out-Null
                $madeChange = $true
                Write-Host "Added <optional>true</optional> in dependency"
            } elseif ($opt.InnerText -ne 'true') {
                $opt.InnerText = 'true'
                $madeChange = $true
                Write-Host "Updated <optional> to true in dependency"
            } else {
                Write-Host "Dependency already optional=true"
            }
        }
        # get version if present
        if ($hasNs) { $vnode = $depNodes[0].SelectSingleNode('m:version', $nsManager) } else { $vnode = $depNodes[0].SelectSingleNode('version') }
        if ($vnode -ne $null) { $versionText = $vnode.InnerText }
    }

    # fallback: check properties for lombok.version
    if (-not $versionText) {
        if ($hasNs) { $prop = selNode("//m:properties/m:lombok.version") } else { $prop = selNode('//properties/lombok.version') }
        if ($prop -ne $null) { $versionText = '${lombok.version}'; Write-Host "Will use property placeholder for version: ${lombok.version}" }
    }

    # find maven-compiler-plugin
    if ($hasNs) { $plugins = selNodes("//m:plugin[m:artifactId='maven-compiler-plugin']") } else { $plugins = selNodes("//plugin[artifactId='maven-compiler-plugin']") }
    if ($plugins -and $plugins.Count -gt 0) {
        foreach ($plugin in $plugins) {
            if ($hasNs) { $config = $plugin.SelectSingleNode('m:configuration', $nsManager) } else { $config = $plugin.SelectSingleNode('configuration') }
            if ($config -eq $null) {
                $config = createElem('configuration')
                $plugin.AppendChild($config) | Out-Null
                $madeChange = $true
                Write-Host "Created <configuration> for maven-compiler-plugin"
            }
            if ($hasNs) { $ann = $config.SelectSingleNode('m:annotationProcessorPaths', $nsManager) } else { $ann = $config.SelectSingleNode('annotationProcessorPaths') }
            if ($ann -eq $null) {
                $ann = createElem('annotationProcessorPaths')
                $config.AppendChild($ann) | Out-Null
                $madeChange = $true
                Write-Host "Created <annotationProcessorPaths>"
            }
            # check existing lombok path
            if ($hasNs) { $exists = $ann.SelectSingleNode("m:path[m:groupId='org.projectlombok' and m:artifactId='lombok']", $nsManager) } else { $exists = $ann.SelectSingleNode("path[groupId='org.projectlombok' and artifactId='lombok']") }
            if ($exists -eq $null) {
                $pathNode = createElem('path')
                $g = createElem('groupId'); $g.InnerText = 'org.projectlombok'
                $a = createElem('artifactId'); $a.InnerText = 'lombok'
                $pathNode.AppendChild($g) | Out-Null
                $pathNode.AppendChild($a) | Out-Null
                if ($versionText) {
                    $v = createElem('version'); $v.InnerText = $versionText
                    $pathNode.AppendChild($v) | Out-Null
                }
                $ann.AppendChild($pathNode) | Out-Null
                $madeChange = $true
                Write-Host "Added lombok to annotationProcessorPaths"
            } else {
                Write-Host "lombok already present in annotationProcessorPaths"
            }
        }
    } else {
        Write-Host "No maven-compiler-plugin found in this POM"
    }

    if ($madeChange) {
        try { $xml.Save($file); $modifiedFiles += $file; Write-Host "Saved changes to $file" } catch { Write-Warning ('Failed to save ' + $file + ': ' + $_) }
    }
}

if ($modifiedFiles.Count -gt 0) {
    Write-Host '\nModified files:\n' $modifiedFiles
    git add @($modifiedFiles)
    $msg = "Make lombok optional and add annotationProcessorPaths for lombok`n`nCo-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
    git commit -m $msg
    Write-Host "Committed changes"
} else {
    Write-Host "No changes made to any files"
}
