# ⛅ android-weather-app

> 現在地の天気をひと目で — シンプルで美しいAndroid天気アプリ

GPS で現在地を自動取得し、リアルタイムの天気・湿度・風速と7日間の天気予報を表示します。
天気コードに応じてグラデーション背景が変化し、予報カードにメモを残す機能も搭載しています。

![Android](https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)

---

## ✨ 特徴

- **現在地の自動取得** — GPS / ネットワーク位置情報で起動時に自動フェッチ
- **天気連動グラデーション** — 晴れ・雨・雪・嵐など天気コードに応じて背景が変化
- **7日間予報** — 横スクロールで最高気温・最低気温・天気アイコンを一覧表示
- **予報メモ機能** — 各日の予報カードをタップしてメモを記録・保存
- **湿度・風速表示** — 現在の詳細気象情報をリアルタイム表示

---

## 🛠️ 技術スタック

| カテゴリ | 技術 |
|---|---|
| 言語 | Kotlin 2.2 |
| UI | Jetpack Compose + Material3 |
| アーキテクチャ | MVVM (ViewModel + StateFlow) |
| 通信 | Retrofit 2 + OkHttp3 + Gson |
| 位置情報 | Google Play Services Location |
| 非同期処理 | Kotlin Coroutines |
| 天気データ | OpenWeatherMap API |

---

## 🚀 セットアップ

```bash
# リポジトリをクローン
git clone https://github.com/masafykun/android-weather-app.git

# Android Studio で開く
# File > Open > android-weather-app/
```

プロジェクトルートに `local.properties` を作成し、APIキーを設定してください。

```properties
sdk.dir=/path/to/your/Android/sdk
WEATHER_API_KEY=your_api_key_here
```

---

## 🔑 環境変数

| 変数名 | 説明 | 必須 |
|---|---|---|
| `WEATHER_API_KEY` | OpenWeatherMap の API キー | ✅ |

> APIキーは [OpenWeatherMap](https://openweathermap.org/api) で無料取得できます。

---

## ライセンス

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](https://opensource.org/licenses/MIT)

このプロジェクトは **MIT ライセンス** のもとで公開しています。

© 2026 masafykun (https://github.com/masafykun)
