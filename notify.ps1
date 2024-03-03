
# Spustí Gradle s příslušnými přepínači a uloží výstup do proměnné
$output = & "./gradlew.bat" clean build --parallel --stacktrace --scan 2>&1

if ($LASTEXITCODE -eq 0) {
    # Gradle build byl úspěšný, takže zobrazí notifikaci s úspěšným výstupem
    New-BurntToastNotification -AppLogo 'C:\Users\nexti\Pictures\idea.png' -Text "Gradle building", "Finished successfully!"
} else {
    # Gradle build selhal, takže zobrazí notifikaci s chybovým výstupem
    New-BurntToastNotification -AppLogo 'C:\Users\nexti\Pictures\idea.png' -Text "Gradle building", "Finished with errors!", "Gradle output:`n$output"
}