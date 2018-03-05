#!/usr/bin/env bash
echo "复制修改过的类到apktool工程..."
cp -f src/main/java/brut/androlib/Androlib.java apktool/brut.apktool/apktool-lib/src/main/java/brut/androlib/Androlib.java
cp -f src/main/java/brut/androlib/ApkOptions.java apktool/brut.apktool/apktool-lib/src/main/java/brut/androlib/ApkOptions.java
cp -f src/main/java/brut/androlib/ApkDecoder.java apktool/brut.apktool/apktool-lib/src/main/java/brut/androlib/ApkDecoder.java
cp -f src/main/java/brut/androlib/res/AndrolibResources.java apktool/brut.apktool/apktool-lib/src/main/java/brut/androlib/res/AndrolibResources.java
cp -f src/main/java/brut/androlib/src/SmaliBuilder.java apktool/brut.apktool/apktool-lib/src/main/java/brut/androlib/src/SmaliBuilder.java
cp -f src/main/java/brut/androlib/src/MainDexConfigParser.java apktool/brut.apktool/apktool-lib/src/main/java/brut/androlib/src/MainDexConfigParser.java
cp -f src/main/java/brut/apktool/Main.java apktool/brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java
cp -f src/main/java/brut/util/OS.java apktool/brut.j.util/src/main/java/brut/util/OS.java

echo "构建jar"
cd apktool
gradle clean
gradle shadowJar
cd ..

echo "把构建出来的jar复制到根目录下"
rm -f ./temp/apktool*.jar
cp -f apktool/brut.apktool/apktool-cli/build/libs/apktool-cli-all.jar ./temp/apktool.jar

if [ "$1" = "-r" ]; then
    echo "回滚apktool工程被修改的类"
    cd apktool
    git checkout -- brut.apktool/apktool-lib/src/main/java/brut/androlib/Androlib.java
    git checkout -- brut.apktool/apktool-lib/src/main/java/brut/androlib/ApkOptions.java
    git checkout -- brut.apktool/apktool-lib/src/main/java/brut/androlib/ApkDecoder.java
    git checkout -- brut.apktool/apktool-lib/src/main/java/brut/androlib/res/AndrolibResources.java
    git checkout -- brut.apktool/apktool-lib/src/main/java/brut/androlib/src/SmaliBuilder.java
    git checkout -- brut.apktool/apktool-cli/src/main/java/brut/apktool/Main.java
    git checkout -- brut.j.util/src/main/java/brut/util/OS.java

    rm brut.apktool/apktool-lib/src/main/java/brut/androlib/src/MainDexConfigParser.java
    find . -name .DS_Store | xargs rm
    cd ..
fi

echo "done"
