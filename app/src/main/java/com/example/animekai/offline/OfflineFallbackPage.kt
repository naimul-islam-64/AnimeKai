package com.example.animekai.offline

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OfflineFallbackPage {

    fun generateHtml(attemptedUrl: String, offlinePages: List<OfflinePage>): String {
        val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

        val pagesListHtml = if (offlinePages.isEmpty()) {
            """
            <div class="empty-box">
                <p>No saved offline pages found.</p>
                <span>Pages are automatically cached into your 10-page library as you stream online.</span>
            </div>
            """.trimIndent()
        } else {
            val items = offlinePages.map { page ->
                val dateStr = dateFormat.format(Date(page.timestamp))
                val sizeKb = page.fileSizeBytes / 1024
                """
                <a href="file://${page.filePath}" class="page-card">
                    <div class="page-info">
                        <div class="page-title">${escapeHtml(page.title)}</div>
                        <div class="page-meta">💾 Cached ${dateStr} • ${sizeKb} KB</div>
                    </div>
                    <div class="page-arrow">➔</div>
                </a>
                """.trimIndent()
            }.joinToString("\n")

            """
            <div class="cached-header">
                <h3>Saved 10-Page Offline Library (${offlinePages.size}/10)</h3>
            </div>
            <div class="pages-list">
                $items
            </div>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <title>AnimeKai - Offline</title>
            <style>
                * {
                    box-sizing: border-box;
                    margin: 0;
                    padding: 0;
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                }
                body {
                    background-color: #0E0C18;
                    color: #F1F0F5;
                    padding: 24px 16px 80px 16px;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    min-height: 100vh;
                }
                .container {
                    width: 100%;
                    max-width: 540px;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                }
                .badge {
                    display: inline-flex;
                    align-items: center;
                    gap: 6px;
                    background: rgba(255, 42, 133, 0.15);
                    color: #FF2A85;
                    border: 1px solid rgba(255, 42, 133, 0.35);
                    padding: 6px 14px;
                    border-radius: 999px;
                    font-size: 12px;
                    font-weight: 700;
                    letter-spacing: 0.5px;
                    text-transform: uppercase;
                    margin-bottom: 16px;
                }
                .badge-dot {
                    width: 8px;
                    height: 8px;
                    border-radius: 50%;
                    background-color: #FF2A85;
                }
                .icon-glow {
                    width: 72px;
                    height: 72px;
                    border-radius: 50%;
                    background: linear-gradient(135deg, rgba(124, 77, 255, 0.2), rgba(0, 229, 255, 0.2));
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    margin-bottom: 16px;
                    border: 1px solid rgba(124, 77, 255, 0.3);
                }
                h1 {
                    font-size: 22px;
                    font-weight: 800;
                    margin-bottom: 8px;
                    text-align: center;
                    background: linear-gradient(90deg, #FFFFFF, #B8B5C7);
                    -webkit-background-clip: text;
                    -webkit-text-fill-color: transparent;
                }
                .subtitle {
                    font-size: 14px;
                    color: #9E9BB2;
                    text-align: center;
                    line-height: 1.5;
                    margin-bottom: 20px;
                }
                .notice-box {
                    background: rgba(124, 77, 255, 0.08);
                    border: 1px solid rgba(124, 77, 255, 0.2);
                    border-radius: 12px;
                    padding: 14px;
                    width: 100%;
                    margin-bottom: 24px;
                    display: flex;
                    gap: 10px;
                    align-items: flex-start;
                }
                .notice-box span {
                    font-size: 18px;
                }
                .notice-box p {
                    font-size: 12.5px;
                    color: #C5C2D9;
                    line-height: 1.45;
                }
                .actions {
                    display: flex;
                    gap: 12px;
                    width: 100%;
                    margin-bottom: 28px;
                }
                .btn {
                    flex: 1;
                    padding: 12px 16px;
                    border-radius: 10px;
                    font-size: 14px;
                    font-weight: 700;
                    text-decoration: none;
                    text-align: center;
                    cursor: pointer;
                    display: inline-flex;
                    align-items: center;
                    justify-content: center;
                    gap: 6px;
                    border: none;
                    transition: transform 0.1s ease;
                }
                .btn:active {
                    transform: scale(0.98);
                }
                .btn-primary {
                    background: linear-gradient(135deg, #7C4DFF, #536DFE);
                    color: white;
                    box-shadow: 0 4px 14px rgba(124, 77, 255, 0.35);
                }
                .btn-secondary {
                    background: rgba(255, 255, 255, 0.08);
                    color: #F1F0F5;
                    border: 1px solid rgba(255, 255, 255, 0.15);
                }
                .cached-header {
                    width: 100%;
                    margin-bottom: 12px;
                }
                .cached-header h3 {
                    font-size: 14px;
                    font-weight: 700;
                    color: #B8B5C7;
                    text-transform: uppercase;
                    letter-spacing: 0.5px;
                }
                .pages-list {
                    width: 100%;
                    display: flex;
                    flex-direction: column;
                    gap: 10px;
                }
                .page-card {
                    background: #1B1829;
                    border: 1px solid rgba(255, 255, 255, 0.08);
                    border-radius: 12px;
                    padding: 14px;
                    text-decoration: none;
                    color: white;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    transition: border-color 0.2s;
                }
                .page-card:active {
                    background: #231F36;
                    border-color: #7C4DFF;
                }
                .page-info {
                    flex: 1;
                    padding-right: 12px;
                }
                .page-title {
                    font-size: 14px;
                    font-weight: 600;
                    color: #F1F0F5;
                    margin-bottom: 4px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    max-width: 280px;
                }
                .page-meta {
                    font-size: 11px;
                    color: #8E8A9F;
                }
                .page-arrow {
                    color: #00E5FF;
                    font-size: 16px;
                }
                .empty-box {
                    width: 100%;
                    padding: 24px;
                    text-align: center;
                    background: #1B1829;
                    border-radius: 12px;
                    border: 1px dashed rgba(255, 255, 255, 0.12);
                }
                .empty-box p {
                    font-size: 14px;
                    font-weight: 600;
                    color: #C5C2D9;
                    margin-bottom: 6px;
                }
                .empty-box span {
                    font-size: 12px;
                    color: #7E7A90;
                }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="badge">
                    <div class="badge-dot"></div>
                    Offline Mode
                </div>

                <div class="icon-glow">
                    <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#00E5FF" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="1" y1="1" x2="23" y2="23"></line>
                        <path d="M16.72 11.06A10.94 10.94 0 0 1 19 12.55"></path>
                        <path d="M5 12.55a10.94 10.94 0 0 1 5.17-2.39"></path>
                        <path d="M10.71 5.05A16 16 0 0 1 22.58 9"></path>
                        <path d="M1.42 9a15.91 15.91 0 0 1 4.7-2.88"></path>
                        <path d="M8.53 16.11a6 6 0 0 1 6.95 0"></path>
                        <line x1="12" y1="20" x2="12.01" y2="20"></line>
                    </svg>
                </div>

                <h1>Page Not Cached Yet</h1>
                <p class="subtitle">You are disconnected from the network and this page is not present in your 10-page offline library.</p>

                <div class="notice-box">
                    <span>⚡</span>
                    <p><strong>Offline Availability:</strong> Episode catalogs, titles, and cached images are accessible offline. Video stream players require an active internet connection.</p>
                </div>

                <div class="actions">
                    <a href="animekai://action/offline_sheet" class="btn btn-primary">
                        📂 Offline Library
                    </a>
                    <a href="animekai://action/retry?url=${escapeHtml(attemptedUrl)}" class="btn btn-secondary">
                        🔄 Retry
                    </a>
                </div>

                $pagesListHtml
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }
}
