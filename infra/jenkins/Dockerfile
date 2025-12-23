FROM jenkins/jenkins:lts-jdk17

# Переключение на root для установки пакетов
USER root

# Установка зависимостей для добавления Docker репозитория
RUN apt-get update && \
    apt-get install -y \
    ca-certificates \
    curl \
    gnupg \
    lsb-release && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Добавление официального GPG ключа Docker
RUN install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg && \
    chmod a+r /etc/apt/keyrings/docker.gpg

# Добавление Docker репозитория
RUN echo \
    "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
    $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Установка Docker CLI и Docker Compose plugin из официального репозитория
RUN apt-get update && \
    apt-get install -y \
    docker-ce-cli \
    docker-compose-plugin \
    gosu \
    && apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Копирование скрипта для исправления прав
COPY fix-permissions.sh /usr/local/bin/fix-permissions.sh
RUN chmod +x /usr/local/bin/fix-permissions.sh

# Установка entrypoint скрипта (выполняется от root для исправления прав)
ENTRYPOINT ["/usr/local/bin/fix-permissions.sh"]

# Оставляем USER root, чтобы entrypoint мог исправить права
# Переключение на jenkins происходит внутри entrypoint скрипта

