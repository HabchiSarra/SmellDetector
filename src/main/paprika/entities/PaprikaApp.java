/*
 * Paprika - Detection of code smells in Android application
 *     Copyright (C)  2016  Geoffrey Hecht - INRIA - UQAM - University of Lille
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Geoffrey Hecht on 20/05/14.
 */
public class PaprikaApp extends Entity{
    private double rating;
    private String date;
    private String pack; //Package
    private int size;
    private String developer;
    private String category;
    private String price;
    private String key;
    private String nbDownload;
    private String versionCode;
    private String versionName;
    private String sdkVersion;
    private String targetSdkVersion;
    private List<PaprikaClass> paprikaClasses;
    private List<PaprikaExternalClass> paprikaExternalClasses;
    private ArrayList<PaprikaLibrary> paprikaLibraries;

    private PaprikaApp(String name, String key, String pack, String date, int size, String developer, String category, String price, double rating, String nbDownload, String versionCode,String versionName,String sdkVersion,String targetSdkVersion) {
        this.name = name;
        this.key = key;
        this.pack = pack;
        this.date = date;
        this.size = size;
        this.developer = developer;
        this.category = category;
        this.price = price;
        this.rating = rating;
        this.nbDownload = nbDownload;
        this.paprikaClasses = new ArrayList<>();
        this.paprikaExternalClasses = new ArrayList<>();
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.sdkVersion = sdkVersion;
        this.targetSdkVersion = targetSdkVersion;
        this.paprikaLibraries = new ArrayList<>();
    }

    private PaprikaApp(String name, String version, String key) {
        this.name=name;
        this.key = key;
        this.versionName = version;
        this.paprikaClasses = new ArrayList<>();
        this.paprikaExternalClasses = new ArrayList<>();
        this.paprikaLibraries = new ArrayList<>();
    }

    public static PaprikaApp createPaprikaApp(String name, String version, String key){
        return new PaprikaApp(name,version,key);
    }

    public List<PaprikaExternalClass> getPaprikaExternalClasses() {
        return paprikaExternalClasses;
    }


    public void addPaprikaExternalClass(PaprikaExternalClass paprikaExternalClass){
        paprikaExternalClasses.add(paprikaExternalClass);
    }

    public List<PaprikaClass> getPaprikaClasses() {
        return paprikaClasses;
    }


    public void addPaprikaClass(PaprikaClass paprikaClass){
        paprikaClasses.add(paprikaClass);
    }

    public static PaprikaApp createPaprikaApp(String name, String key, String pack, String date, int size, String dev, String cat, String price, double rating, String nbDownload, String versionCode,String versionName,String sdkVersion,String targetSdkVersion) {
        return new PaprikaApp(name,key,pack,date,size,dev,cat,price,rating,nbDownload,versionCode,versionName,sdkVersion,targetSdkVersion);
    }

    public double getRating() {
        return rating;
    }

    public String getDate() {
        return date;
    }

    public String getPack() {
        return pack;
    }

    public int getSize() {
        return size;
    }

    public String getDeveloper() {
        return developer;
    }

    public String getCategory() {
        return category;
    }

    public String getPrice() {
        return price;
    }

    public String getKey() {
        return key;
    }

    public String getNbDownload() {
        return nbDownload;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public String getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public ArrayList<PaprikaMethod> getMethods(){
        ArrayList<PaprikaMethod> paprikaMethods = new ArrayList<>();
        for(PaprikaClass paprikaClass: this.getPaprikaClasses()){
            for(PaprikaMethod paprikaMethod: paprikaClass.getPaprikaMethods()){
                paprikaMethods.add(paprikaMethod);
            }
        }
        return paprikaMethods;
    }

    public Entity getPaprikaClass( String className){
        for(PaprikaClass paprikaClass: this.getPaprikaClasses()){
            if(paprikaClass.getName().equals(className)){
                return paprikaClass;
            }
        }
        for(PaprikaExternalClass paprikaExternalClass: this.getPaprikaExternalClasses()){
            if(paprikaExternalClass.getName().equals(className)){
                return paprikaExternalClass;
            }
        }
        return PaprikaExternalClass.createPaprikaExternalClass(className,this);
    }

    public PaprikaClass getPaprikaInternalClass( String className){
        for(PaprikaClass paprikaClass: this.getPaprikaClasses()){
            if(paprikaClass.getName().equals(className)){
                return paprikaClass;
            }
        }
        return null;
    }

    public void addPaprikaLibrary(PaprikaLibrary paprikaLibrary){
        this.paprikaLibraries.add(paprikaLibrary);
    }

    public ArrayList<PaprikaLibrary> getPaprikaLibraries() {
        return paprikaLibraries;
    }
}
