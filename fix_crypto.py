import re
with open("app/src/main/java/com/example/ui/screens/CryptoDetailScreen.kt", "r") as f:
    content = f.read()

# Fix perfGains Box
content = content.replace("""
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 9. RELATED ASSETS ---
""", """
                                }
                            }
                        }
                    }
                    }
                }
            }
        }

        // --- 9. RELATED ASSETS ---
""")

# Actually let's just restore the file and use a smarter approach.
