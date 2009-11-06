package wjhk.jupload2.filedata.helper;

import java.util.HashMap;
import java.util.Map;

import wjhk.jupload2.exception.JUploadException;
import wjhk.jupload2.policies.UploadPolicy;

/**
 * this class is used to parse the
 * {@link UploadPolicy#PROP_TARGET_PICTURE_FORMAT} parameter and provide easy
 * access to the conversions
 * <ul>
 * <li>all file extensions are case-insensitive</li>
 * <li>jpg and jpeg are distinct!</li>
 * </ul>
 * expected format example: {@code "png,bmp:jpg;gif:png;"}
 * 
 * @see UploadPolicy
 */
public class ImageFileConversionInfo {

    private static final String FORMAT_SEPARATOR = ",";

    private static final String RELATION_SEPARATOR = ";";

    private static final String RELATION_ASSIGNMENT = ":";

    /**
     * will only contain strings in lower-case formats
     * <ul>
     * <li>key: source format</li>
     * <li>value: target format</li>
     * </ul>
     */
    private Map<String, String> formatRelations = new HashMap<String, String>();

    /**
     * will build a new ImageFileConversionInfo object for the given
     * conversionList.
     * 
     * @param conversionList e.g. {@code "png,bmp:jpg;gif:png;"}, may be empty
     *            or {@code null}
     * @throws JUploadException if the conversionList is erroneous
     */
    public ImageFileConversionInfo(String conversionList)
            throws JUploadException {
        parseConversionList(conversionList);
    }

    /**
     * returns the target format (in lowercase) for the given sourceFormat or
     * {@code null} if no conversion is necessary (or if sourceFormat is {@code
     * null})
     * 
     * @param sourceFormat format of the source file (case does not matter):
     *            e.g. jpg, JpeG, png, ..
     * @return the target format (in lowercase) for the given sourceFormat or
     *         {@code null} if no conversion is necessary (or if sourceFormat is
     *         {@code null})
     */
    public String getTargetFormatOrNull(String sourceFormat) {
        if (sourceFormat == null) {
            return null;
        }
        String mapValue = this.formatRelations.get(sourceFormat.toLowerCase());
        return mapValue;
    }

    /**
     * returns the target format for the given sourceFormat.
     * <ul>
     * <li>the case of the sourceFormat does not matter</li>
     * <li>the returned format will always be lower-case</li>
     * <li>if a conversion is necessary the target format will be returned: e.g.
     * if "bmp" should be converted to a "png": "png" will be returned</li>
     * <li>if no conversion is necessary, the sourceFormat (in lower-case) will
     * be returned</li>
     * <li>if sourceFormat is {@code null}, {@code null} will be returned</li>
     * </ul>
     * 
     * @param sourceFormat format of the source file (case does not matter):
     *            e.g. jpg, JpeG, png, ..
     * @return the target format for the given sourceFormat (see details in the
     *         method description)
     */
    public String getTargetFormat(String sourceFormat) {
        if (sourceFormat == null) {
            return null;
        }
        String targetFormatOrNull = getTargetFormatOrNull(sourceFormat);
        if (targetFormatOrNull == null) {
            return sourceFormat.toLowerCase();
        }
        return targetFormatOrNull;
    }

    /**
     * will parse the conversion list and fill formatRelations with entries.<br />
     * see description of {@link UploadPolicy} for expected format.
     * 
     * @param conversionList the conversion list to parse
     * @throws JUploadException if problems parsing the conversionList occured
     */
    private void parseConversionList(String conversionList)
            throws JUploadException {
        if (conversionList == null || conversionList.equals("")) {
            return;
        }

        /*
         * example: conversionList="Png,bmp:JPG;gif:png"
         */

        /*
         * if the conversion list does not end with the relation separator, we
         * add it to keep the parsing logic simpler
         */
        if (!conversionList.endsWith(RELATION_SEPARATOR)) {
            conversionList += RELATION_SEPARATOR;
        }

        /*
         * example: conversionList="Png,bmp:JPG;gif:png;"
         */

        String[] relations = conversionList.split(RELATION_SEPARATOR);
        for (String relation : relations) {
            /*
             * example: relation="Png,bmp:JPG"
             */
            String[] assignmentDetails = relation.split(RELATION_ASSIGNMENT);
            if (assignmentDetails.length != 2) {
                throw new JUploadException("Invalid format: relation '"
                        + relation + "' should contain exatly one '"
                        + RELATION_ASSIGNMENT + "'");
            }
            String sourceFormatList = assignmentDetails[0];
            /*
             * example: sourceFormatList="Png,bmp"
             */
            String targetFormat = assignmentDetails[1].toLowerCase();
            /*
             * example: targetFormat="jpg"
             */
            String[] sourceFormats = sourceFormatList.split(FORMAT_SEPARATOR);
            for (String sourceFormat : sourceFormats) {
                /*
                 * example: sourceFormat="Png"
                 */
                String lcSourceFormat = sourceFormat.toLowerCase();
                /*
                 * example: lcSourceFormat="png"
                 */
                if (lcSourceFormat.equals(targetFormat)) {
                    throw new JUploadException("format '" + sourceFormat
                            + "' is assigned to itself");
                }
                String putResult = this.formatRelations.put(lcSourceFormat,
                        targetFormat);
                if (putResult != null) {
                    throw new JUploadException("format '" + lcSourceFormat
                            + "' is assigned to multiple target formats: '"
                            + targetFormat + "', '" + putResult + "'");
                }
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ImageFileConversionInfo (");
        for (Map.Entry<String, String> formatRelation : this.formatRelations
                .entrySet()) {
            sb.append(formatRelation.getKey());
            sb.append("-->");
            sb.append(formatRelation.getValue());
            sb.append(";");
        }
        sb.append(")");

        return sb.toString();
    }
}
