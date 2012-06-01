/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package testsanselan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.kenevans.imagemodel.utils.Utils;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;

public class MetadataExample
{
    private static final String FILENAME = "C:/users/evans/Pictures/ImageBrowser Test/D7A_0670.jpg";

    public static void metadataExample(File file) throws ImageReadException,
        IOException {
        // get all metadata stored in EXIF format (ie. from JPEG or TIFF).
        // org.w3c.dom.Node node = Sanselan.getMetadataObsolete(imageBytes);
        IImageMetadata metadata = Sanselan.getMetadata(file);

        // System.out.println(metadata);

        if(metadata instanceof JpegImageMetadata) {
            JpegImageMetadata jpegMetadata = (JpegImageMetadata)metadata;

            // Jpeg EXIF metadata is stored in a TIFF-based directory structure
            // and is identified with TIFF tags.
            // Here we look for the "x resolution" tag, but
            // we could just as easily search for any other tag.
            //
            // see the TiffConstants file for a list of TIFF tags.
            // BufferedImage exifThumbnail = jpegMetadata.getEXIFThumbnail();
            // TiffImageData rawImageData = jpegMetadata.getRawImageData();

            // findThumbData(jpegMetadata);

            // print out various interesting EXIF tags.
            // printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_XRESOLUTION);
            // printTagValue(jpegMetadata,
            // TiffConstants.EXIF_TAG_EXIF_IMAGE_WIDTH);
            // printTagValue(jpegMetadata,
            // TiffConstants.EXIF_TAG_EXIF_IMAGE_LENGTH);

            // printTagValue(jpegMetadata, TiffConstants.TIFF_TAG_DATE_TIME);
            // printTagValue(jpegMetadata,
            // TiffConstants.EXIF_TAG_DATE_TIME_ORIGINAL);
            // printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_CREATE_DATE);
            // printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_ORIENTATION);
            // printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_ISO);
            // printTagValue(jpegMetadata,
            // TiffConstants.EXIF_TAG_SHUTTER_SPEED_VALUE);
            // printTagValue(jpegMetadata,
            // TiffConstants.EXIF_TAG_APERTURE_VALUE);
            // printTagValue(jpegMetadata,
            // TiffConstants.EXIF_TAG_BRIGHTNESS_VALUE);

            // printTagValue(jpegMetadata,
            // TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
            // printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LATITUDE);
            // printTagValue(jpegMetadata,
            // TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
            // printTagValue(jpegMetadata, TiffConstants.GPS_TAG_GPS_LONGITUDE);

            // printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_ARTIST);
            // printTagValue(jpegMetadata,
            // TiffConstants.EXIF_TAG_CAMERA_SERIAL_NUMBER);
            // printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_COPYRIGHT);
            // printTagValue(jpegMetadata,
            // TiffConstants.EXIF_TAG_DIGITAL_ZOOM_RATIO);
            // printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_EXPOSURE);
            // printTagValue(jpegMetadata, TiffConstants.EXIF_TAG_FOCAL_LENGTH);

            // System.out.println();

            ArrayList<?> items = jpegMetadata.getItems();
            if(items == null) {
                System.out.println("Metadata items is null");
            }
            System.out.println("There are " + items.size() + " metadata items");

            for(int i = 0; i < items.size(); i++) {
                TiffImageMetadata.Item item = (TiffImageMetadata.Item)items
                    .get(i);
                System.out.println("  " + item);

                // TiffField field = item.getTiffField();
                //
                // if (TiffConstants.EXIF_TAG_EXIF_IMAGE_WIDTH.tag==field.tag) {
                // System.out.println(item.getTiffField().getTagName() + ": " +
                // item.getText());
                // }
                // else if
                // (TiffConstants.EXIF_TAG_EXIF_IMAGE_LENGTH.tag==field.tag) {
                // System.out.println(item.getTiffField().getTagName() + ":: " +
                // item.getText());
                // }
                // else if (TiffConstants.TIFF_TAG_DATE_TIME.tag==field.tag) {
                // System.out.println(item.getTiffField().getTagName() + "::: "
                // + item.getText());
                // }

                // System.out.println("    "+item);
                // printTagValue(jpegMetadata, item.getTiffField().tagInfo);

            }

            System.out.println();
        }
    }

    public static byte[] findThumbData(JpegImageMetadata jpegMetadata) {
        byte[] data = null;
        ArrayList<?> dirs = jpegMetadata.getExif().getDirectories();

        for(int i = 0; i < dirs.size(); i++) {
            TiffImageMetadata.Directory tiffdir = (TiffImageMetadata.Directory)dirs
                .get(i);
            if(tiffdir.getJpegImageData() != null) {
                data = tiffdir.getJpegImageData().data;
                if(data != null) {
                    break;
                }
            }
        }
        return data;
    }

    public static void printTagValue(JpegImageMetadata jpegMetadata,
        TagInfo tagInfo) throws ImageReadException, IOException {
        TiffField field = jpegMetadata.findEXIFValue(tagInfo);
        if(field == null)
            System.out.println(tagInfo.name + ": " + "Not Found.");
        else
            System.out.println(tagInfo.name + ": "
                + field.getValueDescription());
    }

    public static void main(String[] args) {
        // String string1 = "I:\\Eigene Bilder\\test2\\46.jpg";
        // String string2 = "M:\\Fotos\\Kathi\\P6300322.JPG";
        // File file = new File("I:\\Eigene BilderTest\\353894.jpg");

        File file = new File(FILENAME);
        System.out.println(file.getPath() + Utils.LS);
        try {
            metadataExample(file);
            //
            // IImageMetadata metadata = Sanselan.getMetadata(new
            // File(string1));
            // if (metadata instanceof JpegImageMetadata) {
            // JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
            // byte[] findThumbData = findThumbData(jpegMetadata);
            // }
        } catch(ImageReadException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

    }

}
