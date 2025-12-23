#!/bin/bash
# Исправление прав на /var/jenkins_home перед запуском Jenkins
set -e

# Исправляем права на /var/jenkins_home (выполняется от root)
if [ -d /var/jenkins_home ]; then
    chown -R jenkins:jenkins /var/jenkins_home
    chmod 755 /var/jenkins_home
fi

# Переключаемся на пользователя jenkins и запускаем оригинальный entrypoint Jenkins
exec gosu jenkins /usr/local/bin/jenkins.sh "$@"

