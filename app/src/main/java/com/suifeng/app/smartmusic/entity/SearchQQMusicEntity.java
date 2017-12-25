package com.suifeng.app.smartmusic.entity;


import java.util.List;

public class SearchQQMusicEntity {

    private int code;
    private DataBean data;
    private String message;
    private String notice;
    private int subcode;
    private int time;
    private String tips;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public int getSubcode() {
        return subcode;
    }

    public void setSubcode(int subcode) {
        this.subcode = subcode;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public static class DataBean {

        private String keyword;
        private int priority;
        private SemanticBean semantic;
        private SongBean song;
        private double totaltime;
        private ZhidaBean zhida;
        private List<?> qc;

        public String getKeyword() {
            return keyword;
        }

        public void setKeyword(String keyword) {
            this.keyword = keyword;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public SemanticBean getSemantic() {
            return semantic;
        }

        public void setSemantic(SemanticBean semantic) {
            this.semantic = semantic;
        }

        public SongBean getSong() {
            return song;
        }

        public void setSong(SongBean song) {
            this.song = song;
        }

        public double getTotaltime() {
            return totaltime;
        }

        public void setTotaltime(double totaltime) {
            this.totaltime = totaltime;
        }

        public ZhidaBean getZhida() {
            return zhida;
        }

        public void setZhida(ZhidaBean zhida) {
            this.zhida = zhida;
        }

        public List<?> getQc() {
            return qc;
        }

        public void setQc(List<?> qc) {
            this.qc = qc;
        }

        public static class SemanticBean {
            /**
             * curnum : 0
             * curpage : 1
             * list : []
             * totalnum : 0
             */

            private int curnum;
            private int curpage;
            private int totalnum;
            private List<?> list;

            public int getCurnum() {
                return curnum;
            }

            public void setCurnum(int curnum) {
                this.curnum = curnum;
            }

            public int getCurpage() {
                return curpage;
            }

            public void setCurpage(int curpage) {
                this.curpage = curpage;
            }

            public int getTotalnum() {
                return totalnum;
            }

            public void setTotalnum(int totalnum) {
                this.totalnum = totalnum;
            }

            public List<?> getList() {
                return list;
            }

            public void setList(List<?> list) {
                this.list = list;
            }
        }

        public static class SongBean {
            private int curnum;
            private int curpage;
            private int totalnum;
            private List<ListBean> list;

            public int getCurnum() {
                return curnum;
            }

            public void setCurnum(int curnum) {
                this.curnum = curnum;
            }

            public int getCurpage() {
                return curpage;
            }

            public void setCurpage(int curpage) {
                this.curpage = curpage;
            }

            public int getTotalnum() {
                return totalnum;
            }

            public void setTotalnum(int totalnum) {
                this.totalnum = totalnum;
            }

            public List<ListBean> getList() {
                return list;
            }

            public void setList(List<ListBean> list) {
                this.list = list;
            }

            public static class ListBean {

                private String albumName_hilight;
                private int chinesesinger;
                private String docid;
                private String f;
                private String fiurl;
                private int fnote;
                private String fsinger;
                private String fsinger2;
                private String fsong;
                private int isupload;
                private int isweiyun;
                private String lyric;
                private String lyric_hilight;
                private String mv;
                private int only;
                private int pubTime;
                private int pure;
                private String singerMID;
                private String singerMID2;
                private String singerName2_hilight;
                private String singerName_hilight;
                private int singerid;
                private int singerid2;
                private String songName_hilight;
                private int t;
                private int tag;
                private int ver;
                private List<GrpBean> grp;

                public String getAlbumName_hilight() {
                    return albumName_hilight;
                }

                public void setAlbumName_hilight(String albumName_hilight) {
                    this.albumName_hilight = albumName_hilight;
                }

                public int getChinesesinger() {
                    return chinesesinger;
                }

                public void setChinesesinger(int chinesesinger) {
                    this.chinesesinger = chinesesinger;
                }

                public String getDocid() {
                    return docid;
                }

                public void setDocid(String docid) {
                    this.docid = docid;
                }

                public String getF() {
                    return f;
                }

                public void setF(String f) {
                    this.f = f;
                }

                public String getFiurl() {
                    return fiurl;
                }

                public void setFiurl(String fiurl) {
                    this.fiurl = fiurl;
                }

                public int getFnote() {
                    return fnote;
                }

                public void setFnote(int fnote) {
                    this.fnote = fnote;
                }

                public String getFsinger() {
                    return fsinger;
                }

                public void setFsinger(String fsinger) {
                    this.fsinger = fsinger;
                }

                public String getFsinger2() {
                    return fsinger2;
                }

                public void setFsinger2(String fsinger2) {
                    this.fsinger2 = fsinger2;
                }

                public String getFsong() {
                    return fsong;
                }

                public void setFsong(String fsong) {
                    this.fsong = fsong;
                }

                public int getIsupload() {
                    return isupload;
                }

                public void setIsupload(int isupload) {
                    this.isupload = isupload;
                }

                public int getIsweiyun() {
                    return isweiyun;
                }

                public void setIsweiyun(int isweiyun) {
                    this.isweiyun = isweiyun;
                }

                public String getLyric() {
                    return lyric;
                }

                public void setLyric(String lyric) {
                    this.lyric = lyric;
                }

                public String getLyric_hilight() {
                    return lyric_hilight;
                }

                public void setLyric_hilight(String lyric_hilight) {
                    this.lyric_hilight = lyric_hilight;
                }

                public String getMv() {
                    return mv;
                }

                public void setMv(String mv) {
                    this.mv = mv;
                }

                public int getOnly() {
                    return only;
                }

                public void setOnly(int only) {
                    this.only = only;
                }

                public int getPubTime() {
                    return pubTime;
                }

                public void setPubTime(int pubTime) {
                    this.pubTime = pubTime;
                }

                public int getPure() {
                    return pure;
                }

                public void setPure(int pure) {
                    this.pure = pure;
                }

                public String getSingerMID() {
                    return singerMID;
                }

                public void setSingerMID(String singerMID) {
                    this.singerMID = singerMID;
                }

                public String getSingerMID2() {
                    return singerMID2;
                }

                public void setSingerMID2(String singerMID2) {
                    this.singerMID2 = singerMID2;
                }

                public String getSingerName2_hilight() {
                    return singerName2_hilight;
                }

                public void setSingerName2_hilight(String singerName2_hilight) {
                    this.singerName2_hilight = singerName2_hilight;
                }

                public String getSingerName_hilight() {
                    return singerName_hilight;
                }

                public void setSingerName_hilight(String singerName_hilight) {
                    this.singerName_hilight = singerName_hilight;
                }

                public int getSingerid() {
                    return singerid;
                }

                public void setSingerid(int singerid) {
                    this.singerid = singerid;
                }

                public int getSingerid2() {
                    return singerid2;
                }

                public void setSingerid2(int singerid2) {
                    this.singerid2 = singerid2;
                }

                public String getSongName_hilight() {
                    return songName_hilight;
                }

                public void setSongName_hilight(String songName_hilight) {
                    this.songName_hilight = songName_hilight;
                }

                public int getT() {
                    return t;
                }

                public void setT(int t) {
                    this.t = t;
                }

                public int getTag() {
                    return tag;
                }

                public void setTag(int tag) {
                    this.tag = tag;
                }

                public int getVer() {
                    return ver;
                }

                public void setVer(int ver) {
                    this.ver = ver;
                }

                public List<GrpBean> getGrp() {
                    return grp;
                }

                public void setGrp(List<GrpBean> grp) {
                    this.grp = grp;
                }

                public static class GrpBean {

                    private String albumName_hilight;
                    private int chinesesinger;
                    private String docid;
                    private String f;
                    private String fiurl;
                    private int fnote;
                    private String fsinger;
                    private String fsinger2;
                    private String fsong;
                    private int isupload;
                    private int isweiyun;
                    private String lyric;
                    private String lyric_hilight;
                    private String mv;
                    private int nt;
                    private int only;
                    private int pubTime;
                    private int pure;
                    private String singerMID;
                    private String singerMID2;
                    private String singerName2_hilight;
                    private String singerName_hilight;
                    private int singerid;
                    private int singerid2;
                    private String songName_hilight;
                    private int t;
                    private int tag;
                    private int ver;

                    public String getAlbumName_hilight() {
                        return albumName_hilight;
                    }

                    public void setAlbumName_hilight(String albumName_hilight) {
                        this.albumName_hilight = albumName_hilight;
                    }

                    public int getChinesesinger() {
                        return chinesesinger;
                    }

                    public void setChinesesinger(int chinesesinger) {
                        this.chinesesinger = chinesesinger;
                    }

                    public String getDocid() {
                        return docid;
                    }

                    public void setDocid(String docid) {
                        this.docid = docid;
                    }

                    public String getF() {
                        return f;
                    }

                    public void setF(String f) {
                        this.f = f;
                    }

                    public String getFiurl() {
                        return fiurl;
                    }

                    public void setFiurl(String fiurl) {
                        this.fiurl = fiurl;
                    }

                    public int getFnote() {
                        return fnote;
                    }

                    public void setFnote(int fnote) {
                        this.fnote = fnote;
                    }

                    public String getFsinger() {
                        return fsinger;
                    }

                    public void setFsinger(String fsinger) {
                        this.fsinger = fsinger;
                    }

                    public String getFsinger2() {
                        return fsinger2;
                    }

                    public void setFsinger2(String fsinger2) {
                        this.fsinger2 = fsinger2;
                    }

                    public String getFsong() {
                        return fsong;
                    }

                    public void setFsong(String fsong) {
                        this.fsong = fsong;
                    }

                    public int getIsupload() {
                        return isupload;
                    }

                    public void setIsupload(int isupload) {
                        this.isupload = isupload;
                    }

                    public int getIsweiyun() {
                        return isweiyun;
                    }

                    public void setIsweiyun(int isweiyun) {
                        this.isweiyun = isweiyun;
                    }

                    public String getLyric() {
                        return lyric;
                    }

                    public void setLyric(String lyric) {
                        this.lyric = lyric;
                    }

                    public String getLyric_hilight() {
                        return lyric_hilight;
                    }

                    public void setLyric_hilight(String lyric_hilight) {
                        this.lyric_hilight = lyric_hilight;
                    }

                    public String getMv() {
                        return mv;
                    }

                    public void setMv(String mv) {
                        this.mv = mv;
                    }

                    public int getNt() {
                        return nt;
                    }

                    public void setNt(int nt) {
                        this.nt = nt;
                    }

                    public int getOnly() {
                        return only;
                    }

                    public void setOnly(int only) {
                        this.only = only;
                    }

                    public int getPubTime() {
                        return pubTime;
                    }

                    public void setPubTime(int pubTime) {
                        this.pubTime = pubTime;
                    }

                    public int getPure() {
                        return pure;
                    }

                    public void setPure(int pure) {
                        this.pure = pure;
                    }

                    public String getSingerMID() {
                        return singerMID;
                    }

                    public void setSingerMID(String singerMID) {
                        this.singerMID = singerMID;
                    }

                    public String getSingerMID2() {
                        return singerMID2;
                    }

                    public void setSingerMID2(String singerMID2) {
                        this.singerMID2 = singerMID2;
                    }

                    public String getSingerName2_hilight() {
                        return singerName2_hilight;
                    }

                    public void setSingerName2_hilight(String singerName2_hilight) {
                        this.singerName2_hilight = singerName2_hilight;
                    }

                    public String getSingerName_hilight() {
                        return singerName_hilight;
                    }

                    public void setSingerName_hilight(String singerName_hilight) {
                        this.singerName_hilight = singerName_hilight;
                    }

                    public int getSingerid() {
                        return singerid;
                    }

                    public void setSingerid(int singerid) {
                        this.singerid = singerid;
                    }

                    public int getSingerid2() {
                        return singerid2;
                    }

                    public void setSingerid2(int singerid2) {
                        this.singerid2 = singerid2;
                    }

                    public String getSongName_hilight() {
                        return songName_hilight;
                    }

                    public void setSongName_hilight(String songName_hilight) {
                        this.songName_hilight = songName_hilight;
                    }

                    public int getT() {
                        return t;
                    }

                    public void setT(int t) {
                        this.t = t;
                    }

                    public int getTag() {
                        return tag;
                    }

                    public void setTag(int tag) {
                        this.tag = tag;
                    }

                    public int getVer() {
                        return ver;
                    }

                    public void setVer(int ver) {
                        this.ver = ver;
                    }
                }
            }
        }

        public static class ZhidaBean {
            /**
             * chinesesinger : 0
             * type : 0
             */

            private int chinesesinger;
            private int type;

            public int getChinesesinger() {
                return chinesesinger;
            }

            public void setChinesesinger(int chinesesinger) {
                this.chinesesinger = chinesesinger;
            }

            public int getType() {
                return type;
            }

            public void setType(int type) {
                this.type = type;
            }
        }
    }

    @Override
    public String toString() {
        return "SearchQQMusicEntity{" +
                "code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                ", notice='" + notice + '\'' +
                ", subcode=" + subcode +
                ", time=" + time +
                ", tips='" + tips + '\'' +
                '}';
    }
}
